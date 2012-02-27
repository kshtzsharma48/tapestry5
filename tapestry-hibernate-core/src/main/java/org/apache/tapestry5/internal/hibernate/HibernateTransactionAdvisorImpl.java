// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.hibernate.HibernateTransactionAdvisor;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.hibernate.annotations.FactoryMarker;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class HibernateTransactionAdvisorImpl implements HibernateTransactionAdvisor
{
    private final HibernateServiceLocator locator;

    public HibernateTransactionAdvisorImpl(HibernateServiceLocator locator)
    {
        this.locator = locator;
    }

    @SuppressWarnings({"unchecked"})
    public void addTransactionCommitAdvice(MethodAdviceReceiver receiver)
    {
        for (Method method : receiver.getInterface().getMethods())
        {
            CommitAfter annotation = method.getAnnotation(CommitAfter.class);

            if (annotation != null)
            {
                receiver.adviseMethod(method, getCommitAfterAdvice(method));
            }
        }
    }

    public MethodAdvice getCommitAfterAdvice(Method method)
    {
        Class<? extends Annotation> marker = getFactoryMarker(method);

        HibernateSessionManager manager = locator.getSessionManagerByMarker(marker);

        return getAdvice(manager);
    }

    private Class<? extends Annotation> getFactoryMarker(Method method)
    {
        Class<? extends Annotation> selected = null;

        for (Class<? extends Annotation> marker : locator.getMarkers())
        {

            Annotation match = method.getAnnotation(marker);
            if (match != null)
            {
                if (selected != null)
                {
                    throw new RuntimeException(
                            HibernateCoreMessages.multipleMarkersNotAllowed(
                                     marker, selected,
                                    method.getDeclaringClass().getName(), method.getName()));
                }
                selected = marker;
            }
        }

        if (selected == null)
        {
            selected = DefaultFactory.class;
        }

        return selected;
    }

    private MethodAdvice getAdvice(final HibernateSessionManager manager)
    {
        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                try
                {
                    invocation.proceed();

                    // Success or checked exception:

                    manager.commit();
                } catch (RuntimeException ex)
                {
                    manager.abort();

                    throw ex;
                }
            }
        };
    }
}
