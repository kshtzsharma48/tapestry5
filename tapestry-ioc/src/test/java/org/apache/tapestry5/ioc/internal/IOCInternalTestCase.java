// Copyright 2006, 2007, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.isA;

public class IOCInternalTestCase extends IOCTestCase implements Registry
{
    private static Registry registry;

    private static PlasticProxyFactory proxyFactory;

    @AfterMethod
    public final void cleanupThread()
    {
        registry.cleanupThread();
    }

    public final PlasticProxyFactory getProxyFactory()
    {
        return proxyFactory;
    }

    public final <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return registry.getObject(objectType, annotationProvider);
    }

    public final <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        return registry.getService(serviceId, serviceInterface);
    }

    public final <T> T getService(Class<T> serviceInterface)
    {
        return registry.getService(serviceInterface);
    }

    public final <T> T getService(Class<T> serviceInterface, Class<? extends Annotation>... markerTypes)
    {
        return registry.getService(serviceInterface, markerTypes);
    }

    public final <T> T autobuild(Class<T> clazz)
    {
        return registry.autobuild(clazz);
    }

    public final <T> T autobuild(String description, Class<T> clazz)
    {
        return registry.autobuild(description, clazz);
    }

    public final void performRegistryStartup()
    {
        registry.performRegistryStartup();
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return registry.proxy(interfaceClass, implementationClass);
    }

    @BeforeSuite
    public final void setup_registry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        registry = builder.build();

        registry.performRegistryStartup();

        proxyFactory = registry.getService(PlasticProxyFactory.class);

    }

    public final void shutdown()
    {
        throw new UnsupportedOperationException("No registry shutdown until @AfterSuite.");
    }

    @AfterSuite
    public final void shutdown_registry()
    {
        registry.shutdown();

        registry = null;
    }

    protected final InternalRegistry mockInternalRegistry()
    {
        return newMock(InternalRegistry.class);
    }

    protected final Module mockModule()
    {
        return newMock(Module.class);
    }

    protected final ObjectCreatorSource mockObjectCreatorSource()
    {
        return newMock(ObjectCreatorSource.class);
    }

    protected final void train_findDecoratorsForService(InternalRegistry registry)
    {
        List<ServiceDecorator> result = Collections.emptyList();

        expect(registry.findDecoratorsForService(isA(ServiceDef3.class))).andReturn(result);
    }

    protected final void train_getDescription(ObjectCreatorSource source, String description)
    {
        expect(source.getDescription()).andReturn(description).atLeastOnce();
    }

    protected final <T> void train_getService(InternalRegistry registry, String serviceId, Class<T> serviceInterface,
                                              T service)
    {
        expect(registry.getService(serviceId, serviceInterface)).andReturn(service);
    }

    protected ServiceActivityTracker mockServiceActivityTracker()
    {
        return newMock(ServiceActivityTracker.class);
    }

    /**
     * @since 5.3
     */
    protected final TypeCoercerProxy mockTypeCoercerProxy()
    {
        return newMock(TypeCoercerProxy.class);
    }

}
