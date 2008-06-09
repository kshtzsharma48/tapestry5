// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.base;

import java.util.Locale;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.TranslatorDefaultSource;
import org.apache.tapestry.services.ValidationMessagesSource;

/**
 * Abstract class for a variety of components that render some variation of a text field. Most of
 * the hooks for user input validation are in this class.
 */
public abstract class AbstractTextField extends AbstractField
{
    /**
     * The value to be read and updated. This is not necessarily a string, a translator may be
     * provided to convert between client side and server side representations. If not bound, a
     * default binding is made to a property of the container matching the component's id. If no
     * such property exists, then you will see a runtime exception due to the unbound value
     * parameter.
     */
    @Parameter(required = true, principal = true)
    private Object _value;

    /**
     * The object which will perform translation between server-side and client-side
     * representations. If not specified, a value will usually be generated based on the type of the
     * value parameter.
     */
    @Parameter(required = true)
    private Translator<Object> _translate;

    /**
     * The object that will perform input validation (which occurs after translation). The translate
     * binding prefix is generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> _validate = NOOP_VALIDATOR;

    @Environmental
    private ValidationTracker _tracker;

    @Inject
    private ValidationMessagesSource _messagesSource;

    @Inject
    private TranslatorDefaultSource _translatorDefaultSource;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Locale _locale;

    @Inject
    private Request _request;

    /**
     * Computes a default value for the "translate" parameter using {@link TranslatorDefaultSource}.
     */
    final Translator defaultTranslate()
    {
        // Because the value parameter is a principal parameter, we know that it will be bound (even
        // via its default parameter) by the time this method is invoked.

        Class type = _resources.getBoundType("value");

        if (type == null) return null;

        return _translatorDefaultSource.find(type);
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link FieldValidatorDefaultSource}.
     */
    final FieldValidator defaultValidate()
    {
        Class type = _resources.getBoundType("value");

        if (type == null) return null;

        return _fieldValidatorDefaultSource.createDefaultValidator(
                this,
                _resources.getId(),
                _resources.getContainerMessages(),
                _locale,
                type,
                _resources.getAnnotationProvider("value"));
    }

    /**
     * The default value is a property of the container whose name matches the component's id. May
     * return null if the container does not have a matching property.
     */
    final Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    @BeginRender
    final void begin(MarkupWriter writer)
    {
        String value = _tracker.getInput(this);

        if (value == null) value = _translate.toClient(_value);

        writeFieldTag(writer, value);

        _validate.render(writer);

        _resources.renderInformalParameters(writer);

        decorateInsideField();
    }

    /**
     * Invoked from {@link #begin(MarkupWriter)} to write out the element and attributes (typically,
     * &lt;input&gt;). The {@link AbstractField#getElementName() elementName} and
     * {@link AbstractField#getClientId() clientId} properties will already have been set or
     * updated.
     * <p>
     * Generally, the subclass will invoke {@link MarkupWriter#element(String, Object[])}, and will
     * be responsible for including an {@link AfterRender} phase method to invoke
     * {@link MarkupWriter#end()}.
     * 
     * @param writer
     *            markup write to send output to
     * @param value
     *            the value (either obtained and translated from the value parameter, or obtained
     *            from the tracker)
     */
    protected abstract void writeFieldTag(MarkupWriter writer, String value);

    @Override
    protected final void processSubmission(FormSupport formSupport, String elementName)
    {
        String rawValue = _request.getParameter(elementName);

        _tracker.recordInput(this, rawValue);

        Messages messages = _messagesSource.getValidationMessages(_locale);

        try
        {
            Object translated = _translate.parseClient(rawValue, messages);

            _validate.validate(translated);

            _value = translated;
        }
        catch (ValidationException ex)
        {
            _tracker.recordError(this, ex.getMessage());
            return;
        }
    }
}