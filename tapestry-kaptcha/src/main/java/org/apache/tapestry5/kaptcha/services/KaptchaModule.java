// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.kaptcha.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Value;
import org.apache.tapestry5.kaptcha.internal.services.KaptchaDataTypeAnalyzer;
import org.apache.tapestry5.kaptcha.internal.services.KaptchaProducerImpl;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

/**
 * Defines core services for Kaptcha support.
 *
 * @since 5.3
 */
public class KaptchaModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(KaptchaProducer.class, KaptchaProducerImpl.class);
    }

    @Contribute(ComponentClassResolver.class)
    public static void provideLibraryMapping(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping(InternalConstants.CORE_LIBRARY, "org.apache.tapestry5.kaptcha"));
    }

    @Contribute(ComponentMessagesSource.class)
    public static void provideLibrayMessages(
            OrderedConfiguration<Resource> configuration,
            @Value("classpath:org/apache/tapestry5/kaptcha/tapestry-kaptcha.properties")
            Resource coreCatalog)
    {
        configuration.add("TapestryKaptcha", coreCatalog, "before:AppCatalog");
    }

    public static void contributeDataTypeAnalyzer(OrderedConfiguration<DataTypeAnalyzer> configuration)
    {
        configuration.add("Kaptcha", new KaptchaDataTypeAnalyzer(), "after:Annotation");
    }

    @Contribute(BeanBlockSource.class)
    public static void provideDefaultBeanBlocks(Configuration<BeanBlockContribution> configuration)
    {
        configuration.add(new EditBlockContribution("kaptcha", "KaptchaEditBlocks", "kaptcha"));

    }
}
