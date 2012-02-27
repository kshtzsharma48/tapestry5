// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateServiceLocator;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.ioc.test.TestBase;
import org.easymock.EasyMock;
import org.hibernate.Session;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.Collection;

@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class HibernateTransactionDecoratorImplTest extends IOCTestCase
{
    private Registry registry;

    private AspectDecorator aspectDecorator;

    private HibernateSessionManager manager;

    private HibernateServiceLocator locator;

    public static class AppModule
    {
        @Marker(DefaultFactory.class)
        public HibernateSessionManager build()
        {
            return EasyMock.createMock(HibernateSessionManager.class);
        }
    }

    @BeforeClass
    public void setup()
    {
        RegistryBuilder builder = new RegistryBuilder();
        builder.add(AppModule.class);
        IOCUtilities.addDefaultModules(builder);
        registry = builder.build();
        aspectDecorator = registry.getService(AspectDecorator.class);

        locator = createHibernateServiceLocator();

        manager = registry.getService(HibernateSessionManager.class);
    }

    @AfterClass
    public void shutdown()
    {
        registry.shutdown();

        aspectDecorator = null;
        registry = null;
    }

    @Test
    public void undecorated()
    {
        VoidService delegate = newMock(VoidService.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.undecorated();

        replay();
        interceptor.undecorated();
        verify();

        assertToString(interceptor);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void void_method()
    {
        VoidService delegate = newMock(VoidService.class);

        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.voidMethod();
        manager.commit();

        replay();
        interceptor.voidMethod();
        verify();

        assertToString(interceptor);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void void_method_with_param()
    {
        VoidService delegate = newMock(VoidService.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.voidMethodWithParam(777);
        manager.commit();

        replay();
        interceptor.voidMethodWithParam(777);
        verify();

        assertToString(interceptor);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void runtime_exception_will_abort_transaction() throws Exception
    {
        Performer delegate = newMock(Performer.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        RuntimeException re = new RuntimeException("Unexpected.");

        delegate.perform();
        TestBase.setThrowable(re);
        manager.abort();

        replay();

        Performer interceptor = decorator.build(Performer.class, delegate, "foo.Bar");

        try
        {
            interceptor.perform();
            TestBase.unreachable();
        } catch (RuntimeException ex)
        {
            Assert.assertSame(ex, re);
        }

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void checked_exception_will_commit_transaction() throws Exception
    {
        Performer delegate = newMock(Performer.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        SQLException se = new SQLException("Checked.");

        delegate.perform();
        TestBase.setThrowable(se);

        manager.commit();

        replay();

        Performer interceptor = decorator.build(Performer.class, delegate, "foo.Bar");

        try
        {
            interceptor.perform();
            TestBase.unreachable();
        } catch (SQLException ex)
        {
            Assert.assertSame(ex, se);
        }

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void return_type_method()
    {
        ReturnTypeService delegate = newTestService();
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        ReturnTypeService interceptor = decorator.build(ReturnTypeService.class, delegate, "foo.Bar");

        delegate.returnTypeMethod();

        manager.commit();

        replay();
        Assert.assertEquals(interceptor.returnTypeMethod(), "Foo");
        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void return_type_method_with_param()
    {
        ReturnTypeService delegate = newTestService();
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(locator);
        ReturnTypeService interceptor = decorator.build(ReturnTypeService.class, delegate, "foo.Bar");

        delegate.returnTypeMethodWithParam(5, 3);
        manager.commit();

        replay();
        Assert.assertEquals(interceptor.returnTypeMethodWithParam(5, 3), 8);
        verify();

        Assert.assertEquals(
                interceptor.toString(),
                "Baz");
    }

    private HibernateServiceLocator createHibernateServiceLocator()
    {
        return new HibernateServiceLocator()
        {
            public Session getSession(Class<?> entityClass)
            {
                return null;
            }

            public Session getSession(String entityName)
            {
                return null;
            }

            public Session getSessionByMarker(Class<? extends Annotation> marker)
            {
                return null;
            }

            public HibernateSessionManager getSessionManager(Class<?> entityClass)
            {
                return null;
            }

            public HibernateSessionManager getSessionManager(String entityName)
            {
                return null;
            }

            public HibernateSessionManager getSessionManagerByMarker(Class<? extends Annotation> marker)
            {
                return manager;
            }

            public Collection<Class<? extends Annotation>> getMarkers()
            {
                return CollectionFactory.newList();
            }
        };
    }

    private HibernateTransactionDecorator newHibernateSessionManagerDecorator(HibernateServiceLocator locator)
    {
        return new HibernateTransactionDecoratorImpl(aspectDecorator,
                new HibernateTransactionAdvisorImpl(locator));
    }

    private void assertToString(VoidService interceptor)
    {
        Assert.assertEquals(
                interceptor.toString(),
                "<Hibernate Transaction interceptor for foo.Bar(" + getClass().getName() + "$VoidService)>");
    }

    private ReturnTypeService newTestService()
    {
        return new ReturnTypeService()
        {

            public String returnTypeMethod()
            {
                return "Foo";
            }

            public int returnTypeMethodWithParam(int first, int second)
            {
                return first + second;
            }

            public String toString()
            {
                return "Baz";
            }
        };
    }

    public interface ReturnTypeService
    {
        @CommitAfter
        String returnTypeMethod();

        @CommitAfter
        int returnTypeMethodWithParam(int first, int second);

        String toString();
    }

    public interface VoidService
    {
        void undecorated();

        @CommitAfter
        void voidMethod();

        @CommitAfter
        void voidMethodWithParam(long id);
    }

    public interface Performer
    {
        @CommitAfter
        void perform() throws SQLException;
    }
}
