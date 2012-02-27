// Copyright 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.hibernate;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Responsible for creating a Hibernate session as needed. Internally, is responsible for Hibernate {@link
 * Configuration}, resulting in a {@link SessionFactory}.
 * <p/>
 * The service's configuration is a {@linkplain org.apache.tapestry5.ioc.services.ChainBuilder chain of command} of
 * configurator objects.
 */
@UsesOrderedConfiguration(HibernateConfigurer.class)
public interface HibernateSessionSource
{
    /**
     * Creates a new session using the {@link #getSessionFactory() SessionFactory} created at service startup.
     */
    Session create();

    /**
     * Creates a new session using the {@link #getSessionFactory(Class)}
     */
    Session create(Class<? extends Annotation> marker);

    /**
     * Returns the default SessionFactory from which Hibernate sessions are created.
     * This is equivalent to calling {@link #getSessionFactory(Class)} with
     * {@link org.apache.tapestry5.hibernate.annotations.DefaultFactory} as argument
     *
     * @return default session factory
     */
    SessionFactory getSessionFactory();

    /**
     * Returns the SessionFactory for a particular factoryId.
     *
     * @param marker
     * @return SessionFactory for this particular session id
     */
    SessionFactory getSessionFactory(Class<? extends Annotation> marker);

    /**
     * Returns the SessionFactory associated with an particular entity
     *
     * @param entityClass
     * @return
     */
    SessionFactory getSessionFactoryByEntityClass(Class<?> entityClass);

    /**
     * Returns the final default configuration used to create the {@link SessionFactory}. The
     * configuration is immutable.
     */
    Configuration getConfiguration();

    /**
     * Returns configuration of a particular factoryId
     *
     * @param marker
     * @return configuration
     */
    Configuration getConfiguration(Class<? extends Annotation> marker);

    /**
     * Get factory id to which the given entity belongs
     *
     * @param entityClass
     * @return factory marker
     */
    Class<? extends Annotation> getFactoryMarker(Class<?> entityClass);

    /**
     * Get factory id to which the given entity belongs
     *
     * @param entityName entity name
     * @return factory id
     */
    Class<? extends Annotation> getFactoryMarker(String entityName);

    Collection<Class<? extends Annotation>> getFactoryMarkers();

    /**
     * Get configurations
     * 
     * @return
     */
    Collection<Configuration> getConfigurations();
}
