package org.apache.tapestry5.hibernate;

import org.hibernate.Session;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface HibernateServiceLocator
{
    Session getSession(Class<?> entityClass);

    Session getSession(String entityName);

    Session getSessionByMarker(Class<? extends Annotation> marker);

    HibernateSessionManager getSessionManager(Class<?> entityClass);

    HibernateSessionManager getSessionManager(String entityName);

    HibernateSessionManager getSessionManagerByMarker(Class<? extends Annotation> marker);

    Collection<Class<? extends Annotation>> getMarkers();
}
