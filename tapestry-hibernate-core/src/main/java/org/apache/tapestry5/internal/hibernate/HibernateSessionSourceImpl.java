// Copyright 2007, 2008, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.hibernate.HibernateSymbols;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@UsesConfiguration(OrderedConfiguration.class)
public class HibernateSessionSourceImpl implements HibernateSessionSource
{
    private static class FactoryConfiguration
    {
        private final SessionFactory sessionFactory;

        private final Configuration configuration;

        public FactoryConfiguration(SessionFactory sessionFactory,
                                    Configuration configuration)
        {
            this.sessionFactory = sessionFactory;
            this.configuration = configuration;
        }

        public SessionFactory getSessionFactory()
        {
            return sessionFactory;
        }

        public Configuration getConfiguration()
        {
            return configuration;
        }
    }

    private final Map<Class<? extends Annotation>, FactoryConfiguration> factoryConfigurations =
            CollectionFactory.newMap();

    private final FactoryConfiguration defaultFactoryConfiguration;

    private final Map<String, Class<? extends Annotation>> classNameToFactoryMap =
            CollectionFactory.newMap();

    private final ClassNameLocator classNameLocator;

    public HibernateSessionSourceImpl(Logger logger,
                                      ClassNameLocator classNameLocator,
                                      @Symbol(HibernateSymbols.DEFAULT_CONFIGURATION)
                                      boolean defaultConfigurationRequired,
                                      List<HibernateConfigurer> hibernateConfigurers)
    {
        this.classNameLocator = classNameLocator;

        Map<Class<? extends Annotation>, List<HibernateConfigurer>> configurerMap =
                combineConfigurersByFactoryId(hibernateConfigurers);

        for (Class<? extends Annotation> marker : configurerMap.keySet())
        {
            List<HibernateConfigurer> configurers = configurerMap.get(marker);

            long startTime = System.currentTimeMillis();

            Configuration configuration = configure(configurers);

            loadEntities(configuration, configurers, marker);

            long configurationComplete = System.currentTimeMillis();
            if (marker.equals(DefaultFactory.class) && defaultConfigurationRequired)
            {
                configuration.configure();
            }

            SessionFactory sessionFactory = configuration.buildSessionFactory();

            factoryConfigurations.put(marker,
                    new FactoryConfiguration(sessionFactory, configuration));

            long factoryCreated = System.currentTimeMillis();

            logger.info(HibernateCoreMessages.startupTiming(configurationComplete - startTime,
                    factoryCreated - startTime));

            logger.info(
                    HibernateCoreMessages.entityCatalog(sessionFactory.getAllClassMetadata().keySet()
                    ));
        }

        if (factoryConfigurations.size() == 0)
        {
            throw new RuntimeException(HibernateCoreMessages.noConfigurationFound());
        }

        defaultFactoryConfiguration = factoryConfigurations.get(DefaultFactory.class);
    }

    private Map<Class<? extends Annotation>, List<HibernateConfigurer>>
    combineConfigurersByFactoryId(
            List<HibernateConfigurer> hibernateConfigurers)
    {
        Map<Class<? extends Annotation>, List<HibernateConfigurer>> configurerMap =
                CollectionFactory.newMap();

        for (HibernateConfigurer configurer : hibernateConfigurers)
        {
            Class<? extends Annotation> marker = configurer.getMarker();

            if (!configurerMap.containsKey(marker))
            {
                configurerMap.put(marker, new ArrayList<HibernateConfigurer>());
            }
            configurerMap.get(marker).add(configurer);
        }

        return configurerMap;
    }

    private void loadEntities(Configuration configuration,
                              List<HibernateConfigurer> configurers,
                              Class<? extends Annotation> marker)
    {
        for (HibernateConfigurer configurer : configurers)
        {
            String[] packageNames = configurer.getPackageNames();

            if (packageNames != null)
            {
                for (String packageName : packageNames)
                {
                    loadEntitiesFromPackage(configuration, packageName, marker);
                }
            }
        }
    }

    private void loadEntitiesFromPackage(Configuration configuration,
                                         String packageName,
                                         Class<? extends Annotation> marker)
    {
        for (String className : classNameLocator.locateClassNames(packageName))
        {
            try
            {
                Class entityClass = Thread.currentThread().getContextClassLoader().
                        loadClass(className);

                configuration.addAnnotatedClass(entityClass);

                classNameToFactoryMap.put(className, marker);
            } catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    private Configuration configure(List<HibernateConfigurer> configurers)
    {
        Configuration configuration = new Configuration();
        for (HibernateConfigurer configurer : configurers)
        {
            configurer.configure(configuration);
        }

        return configuration;
    }


    @PostInjection
    public void listenForShutdown(RegistryShutdownHub hub)
    {
        hub.addRegistryShutdownListener(new Runnable()
        {
            public void run()
            {
                for (FactoryConfiguration configuration : factoryConfigurations.values())
                {
                    configuration.getSessionFactory().close();
                }
            }
        });
    }

    public Session create()
    {
        return getSessionFactory().openSession();
    }

    public Session create(Class<? extends Annotation> marker)
    {
        return getSessionFactory(marker).openSession();
    }

    private void ensureFactoryExists(Class<? extends Annotation> marker)
    {
        if (!factoryConfigurations.containsKey(marker))
        {
            throw new IllegalArgumentException(HibernateCoreMessages.factoryDoesNotExist(
                    marker.getName()));
        }
    }

    public SessionFactory getSessionFactory()
    {
        ensureFactoryExists(DefaultFactory.class);
        return defaultFactoryConfiguration.getSessionFactory();
    }

    public SessionFactory getSessionFactory(Class<? extends Annotation> marker)
    {
        ensureFactoryExists(marker);
        return factoryConfigurations.get(marker).getSessionFactory();
    }

    public SessionFactory getSessionFactoryByEntityClass(Class<?> entityClass)
    {
        return getSessionFactory(getFactoryMarker(entityClass));
    }

    public Configuration getConfiguration()
    {
        ensureFactoryExists(DefaultFactory.class);
        return defaultFactoryConfiguration.getConfiguration();
    }

    public Configuration getConfiguration(Class<? extends Annotation> marker)
    {
        ensureFactoryExists(marker);
        return factoryConfigurations.get(marker).getConfiguration();
    }

    public Class<? extends Annotation> getFactoryMarker(Class<?> entityClass)
    {
        return getFactoryMarker(entityClass.getName());
    }

    public Class<? extends Annotation> getFactoryMarker(String entityName)
    {
        if (!classNameToFactoryMap.containsKey(entityName))
        {
            throw new IllegalArgumentException(HibernateCoreMessages.entityNotBound(entityName));
        }

        return classNameToFactoryMap.get(entityName);
    }

    public Collection<Class<? extends Annotation>> getFactoryMarkers()
    {
        return factoryConfigurations.keySet();
    }

    public Collection<Configuration> getConfigurations()
    {
        Collection<Configuration> collection = CollectionFactory.newList();
        for(FactoryConfiguration factoryConfiguration: factoryConfigurations.values()){
            collection.add(factoryConfiguration.getConfiguration());
        }
        return collection;
    }

}
