package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateServiceLocator;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public class HibernateServiceLocatorImpl implements HibernateServiceLocator
{
    private ObjectLocator locator;

    private HibernateSessionSource source;

    private static final Logger logger = LoggerFactory.getLogger(HibernateServiceLocatorImpl.class);

    public HibernateServiceLocatorImpl(ObjectLocator locator, HibernateSessionSource source)
    {
        this.locator = locator;
        this.source = source;
    }

    public Session getSession(Class<?> entityClass)
    {
        return getSession(entityClass.getName());
    }

    @SuppressWarnings({"unchecked"})
    public Session getSession(String entityName)
    {
        return locator.getService(Session.class, source.getFactoryMarker(entityName));
    }

    @SuppressWarnings({"unchecked"})
    public Session getSessionByMarker(Class<? extends Annotation> marker)
    {
        return locator.getService(Session.class, marker);
    }

    public HibernateSessionManager getSessionManager(Class<?> entityClass)
    {
        return getSessionManager(entityClass.getName());
    }

    @SuppressWarnings({"unchecked"})
    public HibernateSessionManager getSessionManager(String entityName)
    {
        return locator.getService(HibernateSessionManager.class,
                source.getFactoryMarker(entityName));
    }

    @SuppressWarnings({"unchecked"})
    public HibernateSessionManager getSessionManagerByMarker(Class<? extends Annotation> marker)
    {
        return locator.getService(HibernateSessionManager.class, marker);
    }

    public Collection<Class<? extends Annotation>> getMarkers()
    {
        return source.getFactoryMarkers();
    }
}
