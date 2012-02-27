// Copyright 2008, 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.internal.hibernate.HibernateServiceLocatorImpl;
import org.apache.tapestry5.internal.hibernate.HibernateSessionManagerImpl;
import org.apache.tapestry5.internal.hibernate.HibernateSessionSourceImpl;
import org.apache.tapestry5.internal.hibernate.HibernateTransactionAdvisorImpl;
import org.apache.tapestry5.internal.hibernate.HibernateTransactionDecoratorImpl;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.hibernate.Session;

/**
 * Defines core services that support initialization of Hibernate and access to the Hibernate
 * {@link
 * org.hibernate.Session}.
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
@Marker(HibernateCore.class)
public class HibernateCoreModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(HibernateTransactionDecorator.class, HibernateTransactionDecoratorImpl.class);
        binder.bind(HibernateSessionSource.class, HibernateSessionSourceImpl.class);
        binder.bind(HibernateServiceLocator.class, HibernateServiceLocatorImpl.class);
    }


    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateSymbols.DEFAULT_CONFIGURATION, "true");
        configuration.add(HibernateSymbols.EARLY_START_UP, "false");
    }

    public static void contributeRegistryStartup(OrderedConfiguration<Runnable> configuration,
                                                 @Symbol(HibernateSymbols.EARLY_START_UP)
                                                 final boolean earlyStartup,

                                                 final HibernateSessionSource sessionSource)
    {
        configuration.add("HibernateStartup", new Runnable()
        {
            public void run()
            {
                if (earlyStartup)
                    sessionSource.getConfigurations();
            }
        });
    }

    @Marker(DefaultFactory.class)
    public static HibernateTransactionAdvisor buildHibernateTransactionAdvisor(
            HibernateServiceLocator locator)
    {
        return new HibernateTransactionAdvisorImpl(locator);
    }

    /**
     * The session manager manages sessions on a per-thread/per-request basis. Any active
     * transaction will be rolled
     * back at {@linkplain org.apache.tapestry5.ioc.Registry#cleanupThread() thread cleanup time}
     * .  The thread is
     * cleaned up automatically in a Tapestry web application.
     */
    @Scope(ScopeConstants.PERTHREAD)
    @Marker(DefaultFactory.class)
    public static HibernateSessionManager buildHibernateSessionManager(
            HibernateSessionSource sessionSource,
            PerthreadManager perthreadManager)
    {
        HibernateSessionManagerImpl service = new HibernateSessionManagerImpl(sessionSource,
                DefaultFactory.class);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    @Marker(DefaultFactory.class)
    public static Session buildSession(
            @Local HibernateSessionManager sessionManager,
            PropertyShadowBuilder propertyShadowBuilder)
    {
        // Here's the thing: the tapestry.hibernate.Session class doesn't have to be per-thread,
        // since it will invoke getSession() on the HibernateSessionManager
        // service (which is per-thread). On first invocation per request,
        // this forces the HSM into existence (which creates the session and begins the
        // transaction). Thus we don't actually create a session until we first try to access
        // it, then the session continues to exist for the rest of the request.

        return propertyShadowBuilder.build(sessionManager, "session", Session.class);
    }

}
