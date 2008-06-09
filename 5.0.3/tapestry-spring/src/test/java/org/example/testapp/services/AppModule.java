// Copyright 2007 The Apache Software Foundation
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

package org.example.testapp.services;

import org.apache.tapestry.ioc.annotations.SubModule;
import org.apache.tapestry.spring.SpringModule;

/**
 * The SubModule is not normally needed, except that during tests of tapestry-spring, the necessary
 * JAR Manifest does not yet exist, so we force the tapestry.spring module into the registry
 * explicitly.
 */
@SubModule(SpringModule.class)
public class AppModule
{

}