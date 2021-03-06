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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.integration.app1.data.SubscribeData;

public class FormFragmentDemo
{
    @Property
    private SubscribeData subscribe;

    @Property
    private boolean subscribeToEmail, codeVisible, subVisible = true;

    @Component(parameters =
            {"clientValidation=false"})
    private Form form;

    @InjectPage
    private FormFragmentOutput outputPage;

    void onPrepare()
    {
        subscribe = new SubscribeData();
    }

    void onActionFromClear()
    {
        form.clearErrors();
    }

    Object onFailure()
    {
        throw new RuntimeException("Show me the Request!");
    }

    Object onSuccess()
    {
        return outputPage.initialize(subscribe);
    }
}
