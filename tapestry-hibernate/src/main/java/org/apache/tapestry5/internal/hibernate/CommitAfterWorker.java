// Copyright 2008, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.hibernate.HibernateServiceLocator;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.lang.annotation.Annotation;

/**
 * Searches for methods that have the {@link org.apache.tapestry5.hibernate.annotations
 * .CommitAfter} annotation and adds
 * logic around the method to commit or abort the transaction. The commit/abort logic is the same
 * as for the
 * {@link org.apache.tapestry5.hibernate.HibernateTransactionDecorator} service.
 */
public class CommitAfterWorker implements ComponentClassTransformWorker2
{
    private HibernateServiceLocator locator;

    public CommitAfterWorker(HibernateServiceLocator locator)
    {
        this.locator = locator;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support,
                          MutableComponentModel model)
    {
        for (PlasticMethod method : plasticClass.getMethodsWithAnnotation(CommitAfter.class))
        {
            method.addAdvice(getCommitAfterAdvice(method));
        }
    }

    public MethodAdvice getCommitAfterAdvice(PlasticMethod method)
    {
        Class<? extends Annotation> marker = getFactoryMarker(method);

        HibernateSessionManager manager = locator.getSessionManagerByMarker(marker);

        return getAdvice(manager);
    }

    private Class<? extends Annotation> getFactoryMarker(PlasticMethod method)
    {
        Class<? extends Annotation> selected = null;

        for (Class<? extends Annotation> marker : locator.getMarkers())
        {
            if (method.hasAnnotation(marker))
            {
                if (selected != null)
                {
                    throw new RuntimeException(
                            HibernateCoreMessages.multipleMarkersNotAllowed(
                                    marker, selected,
                                    method.getPlasticClass().getClassName(), method.getDescription().methodName));
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
