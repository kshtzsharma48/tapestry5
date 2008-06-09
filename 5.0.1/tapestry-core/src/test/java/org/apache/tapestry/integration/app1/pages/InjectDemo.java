// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.Request;

@ComponentClass
public class InjectDemo
{
    // Named
    @Inject("infrastructure:Request")
    private Request _request;

    // Via ComponentResourcesInjectionProvider
    @Inject
    private ComponentResources _resources;

    // Via DefaultInjectionProvider -- have to ensure that BindingSource
    // stays unique.
    @Inject
    private BindingSource _bindingSource;

    @InjectPage
    private Fred _fred;

    @InjectPage("Barney")
    private Runnable _barney;

    public BindingSource getBindingSource()
    {
        return _bindingSource;
    }

    public Request getRequest()
    {
        return _request;
    }

    public ComponentResources getResources()
    {
        return _resources;
    }

    @OnEvent(component = "fred")
    Object clickFred()
    {
        return _fred;
    }

    @OnEvent(component = "barney")
    Object clickBarney()
    {
        return _barney;
    }

    @OnEvent(component = "wilma")
    String clickWilma()
    {
        return "Wilma";
    }
}