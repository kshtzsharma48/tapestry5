// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.mixins.DiscardBody;

import java.util.regex.Pattern;

/**
 * Outputs paragraph oriented text, typically collected via a {@link org.apache.tapestry.corelib.components.TextArea}
 * component.  The TextArea is split into lines, and each line it output inside its own &lt;p&gt; element.
 */
public class TextOutput
{
    @Parameter(required = true)
    private String _value;

    @Mixin
    private DiscardBody _discardBody;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("((\\r\\n)|\\r|\\n)", Pattern.MULTILINE);

    void beginRender(MarkupWriter writer)
    {
        if (_value == null) return;

        String[] lines = SPLIT_PATTERN.split(_value);

        for (String line : lines)
        {
            writer.element("p");

            writer.write(line.trim());

            writer.end();
        }
    }

    void injectValue(String value)
    {
        _value = value;
    }
}