// Copyright 2006, 2007, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.transform.ReadOnlyComponentFieldConduit;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;

/**
 * Allows for a number of anonymous injections based on the type of field that is to be injected.
 */
public class CommonResourcesInjectionProvider implements InjectionProvider2
{
    interface ResourceProvider<T>
    {
        T get(ComponentResources resources);
    }

    static class ResourceConduit extends ReadOnlyComponentFieldConduit
    {
        private final ResourceProvider<?> provider;

        ResourceConduit(String className, String fieldName, ResourceProvider<?> provider)
        {
            super(className, fieldName);
            this.provider = provider;
        }

        public Object get(Object instance, InstanceContext context)
        {
            ComponentResources resources = context.get(ComponentResources.class);

            return provider.get(resources);
        }
    }

    private static ResourceProvider<ComponentResourceSelector> selectorProvider = new ResourceProvider<ComponentResourceSelector>()
    {
        public ComponentResourceSelector get(ComponentResources resources)
        {
            return resources.getResourceSelector();
        }
    };

    private static ResourceProvider<Messages> messagesProvider = new ResourceProvider<Messages>()
    {

        public Messages get(ComponentResources resources)
        {
            return resources.getMessages();
        }
    };

    private static ResourceProvider<Locale> localeProvider = new ResourceProvider<Locale>()
    {

        public Locale get(ComponentResources resources)
        {
            return resources.getLocale();
        }
    };

    private static ResourceProvider<Logger> loggerProvider = new ResourceProvider<Logger>()
    {

        public Logger get(ComponentResources resources)
        {
            return resources.getLogger();
        }

        ;
    };

    private static ResourceProvider<String> completeIdProvider = new ResourceProvider<String>()
    {
        public String get(ComponentResources resources)
        {
            return resources.getCompleteId();
        }
    };

    private static final Map<String, ResourceProvider> configuration = CollectionFactory.newMap();

    {
        configuration.put(ComponentResourceSelector.class.getName(), selectorProvider);
        configuration.put(Messages.class.getName(), messagesProvider);
        configuration.put(Locale.class.getName(), localeProvider);
        configuration.put(Logger.class.getName(), loggerProvider);
        configuration.put(String.class.getName(), completeIdProvider);
    }

    public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
    {
        ResourceProvider provider = configuration.get(field.getTypeName());

        if (provider == null)
        {
            return false;
        }

        field.setConduit(new ResourceConduit(field.getPlasticClass().getClassName(), field.getName(), provider));

        return true;
    }
}
