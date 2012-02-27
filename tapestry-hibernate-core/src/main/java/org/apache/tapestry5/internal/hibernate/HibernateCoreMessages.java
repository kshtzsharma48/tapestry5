// Copyright 2008, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class HibernateCoreMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(HibernateCoreMessages.class);

    public static String startupTiming(long toConfigure, long overall)
    {
        return MESSAGES.format("startup-timing", toConfigure, overall);
    }

    public static String entityCatalog(Collection entityNames)
    {
        return MESSAGES.format("entity-catalog", InternalUtils.joinSorted(entityNames));
    }

    public static String factoryDoesNotExist(String factoryId)
    {
        return MESSAGES.format("factory-does-not-exist", factoryId);
    }

    public static String entityNotBound(String entityName)
    {
        return MESSAGES.format("entity-not-bound", entityName);
    }

    public static String noConfigurationFound()
    {
        return MESSAGES.format("no-configuration-found");
    }

    public static String noDefaultConfigurationFound()
    {
        return MESSAGES.format("no-default-configuration-found");
    }

    public static String multipleMarkersNotAllowed(
            Class<? extends Annotation> match,
            Class<? extends Annotation> selected,
            String className,
            String methodName)
    {
        return MESSAGES.format("multiple-markers-not-allowed", className, methodName,
                selected.getCanonicalName(), match.getCanonicalName());
    }
}
