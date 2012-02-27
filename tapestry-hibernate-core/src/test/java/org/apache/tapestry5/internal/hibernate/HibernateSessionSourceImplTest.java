// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.internal.services.ClassNameLocatorImpl;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.example.app0.entities.User;
import org.example.app0.entities.services.AppModule;
import org.example.app1.NonDefaultFactory;
import org.example.app1.entities.User2;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.util.Properties;

public class HibernateSessionSourceImplTest extends IOCTestCase
{
    private final Logger logger = LoggerFactory.getLogger(
            "tapestry.hibernate.HibernateSessionSourceTest");

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "No hibernate configuration found")
    public void startup_with_no_configurer_throws_exception()
    {
        ClassNameLocator classNameLocator = mockClassNameLocator();

        @SuppressWarnings({"UnusedDeclaration"})
        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.<HibernateConfigurer>newList());

    }

    @Test
    public void startup_with_custom_hibernate_configuration_works()
    {
        RegistryBuilder builder = new RegistryBuilder();
        builder.add(AppModule.class);

        IOCUtilities.addDefaultModules(builder);

        Registry registry = builder.build();

        Session session = registry.getService(Session.class);
        User user = new User();
        session.save(user);
    }

    @Test
    public void startup_with_default_configurer_and_default_configuration_true_uses_xml()
    {
        ClassNameLocator classNameLocator = new ClassNameLocatorImpl(new
                ClasspathURLConverterImpl());

        HibernateConfigurer defaultConfigurer = new HibernateConfigurer()
        {
            public void configure(Configuration configuration)
            {

            }

            public Class<? extends Annotation> getMarker()
            {
                return DefaultFactory.class;
            }

            public String[] getPackageNames()
            {
                return new String[]{"org.example.app0.entities"};
            }
        };

        @SuppressWarnings({"UnusedDeclaration"})
        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                true, CollectionFactory.newList(defaultConfigurer));

        assertNotNull(source.getConfiguration());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void startup_with_no_default_configurer_throws_exception_if_default_configuration_is_accessed()
    {
        ClassNameLocator classNameLocator = mockClassNameLocator();

        HibernateConfigurer nonDefaultConfigurer = createConfigurer(new String[0], NonDefaultFactory.class);

        @SuppressWarnings({"UnusedDeclaration"})
        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(nonDefaultConfigurer));

        source.getConfiguration();
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void startup_with_no_default_configurer_throws_exception_if_default_sf_is_accessed()
    {
        ClassNameLocator classNameLocator = mockClassNameLocator();

        HibernateConfigurer nonDefaultConfigurer = createConfigurer(new String[0], NonDefaultFactory.class);

        @SuppressWarnings({"UnusedDeclaration"})
        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(nonDefaultConfigurer));

        source.getSessionFactory();
    }


    @Test
    public void startup_with_default_configurer_sets_up_configuration()
    {
        ClassNameLocator classNameLocator = mockClassNameLocator();

        HibernateConfigurer defaultConfigurer = createConfigurer(new String[0],
                DefaultFactory.class);

        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(defaultConfigurer));

        assertNotNull(source.getConfiguration());
        assertNotNull(source.getConfiguration(DefaultFactory.class));
    }

    @Test
    public void startup_with_default_configurer_maps_all_package_entities()
    {
        ClassNameLocator classNameLocator = new ClassNameLocatorImpl(new
                ClasspathURLConverterImpl());

        HibernateConfigurer defaultConfigurer = createConfigurer(
                new String[]{
                        "org.example.app0.entities"
                }, DefaultFactory.class);

        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(defaultConfigurer));

        assertNotNull(source.getConfiguration().getClassMapping(User.class.getName()));
        assertNotNull(source.getSessionFactoryByEntityClass(User.class));
    }

    @Test
    public void startup_with_multiple_configurers()
    {
        ClassNameLocator classNameLocator = new ClassNameLocatorImpl(new
                ClasspathURLConverterImpl());

        HibernateConfigurer defaultConfigurer = createConfigurer(
                new String[]{
                        "org.example.app0.entities"
                }, DefaultFactory.class);

        HibernateConfigurer nonDefaultConfigurer = createConfigurer(
                new String[]{
                        "org.example.app1.entities"
                }, NonDefaultFactory.class);

        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(defaultConfigurer, nonDefaultConfigurer));

        assertNotNull(source.getConfiguration().getClassMapping(User.class.getName()));
        assertNotNull(source.getSessionFactoryByEntityClass(User.class));

        assertNotNull(source.getConfiguration(NonDefaultFactory.class).getClassMapping(
                User2.class.getName()
        ));

        assertNotNull(source.getSessionFactoryByEntityClass(User2.class));
        assertTrue(source.getSessionFactory() == source.getSessionFactoryByEntityClass(User.class));
        assertTrue(source.getSessionFactory() != source.getSessionFactoryByEntityClass(User2.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Entity : java.lang.String is not bound to any of the configured session factories")
    public void factory_id_does_not_exist()
    {
        ClassNameLocator classNameLocator = new ClassNameLocatorImpl(new
                ClasspathURLConverterImpl());

        HibernateConfigurer defaultConfigurer = createConfigurer(new String[]{"org.example.app0.entities"}, DefaultFactory.class);

        HibernateSessionSource source = new HibernateSessionSourceImpl(logger, classNameLocator,
                false, CollectionFactory.newList(defaultConfigurer));

        source.getFactoryMarker(String.class);
    }


    private HibernateConfigurer createConfigurer(final String[] packageNames, final Class<? extends Annotation> marker)
    {
        return new HibernateConfigurer()
        {
            public void configure(Configuration configuration)
            {
                Properties properties = new Properties();
                properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
                properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
                properties.put("hibernate.connection.username", "sa");
                properties.put("hibernate.connection.password", "");
                properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

                configuration.setProperties(properties);
            }

            public Class<? extends Annotation> getMarker()
            {
                return marker;
            }

            public String[] getPackageNames()
            {
                return packageNames;
            }
        };
    }

    private ClassNameLocator mockClassNameLocator()
    {
        return newMock(ClassNameLocator.class);
    }


}
