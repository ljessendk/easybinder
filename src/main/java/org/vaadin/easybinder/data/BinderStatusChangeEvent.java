/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Modifications Copyright 2017 Lars Sønderby Jessen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.easybinder.data;
import java.util.EventObject;

/**
 * Binder status change event.
 * <p>
 * The {@link BasicBinder} status is changed whenever any of the following happens:
 * <ul>
 * <li>if any of its bound fields or selects have been changed
 * <li>{@link BasicBinder#setBean(Object)} is called
 * <li>{@link BasicBinder#removeBean()} is called
 * <li>{@link BasicBinder#bind(HasValue, ValueProvider, Setter, String)} is called
 * </ul>
 *
 * @see BinderStatusChangeListener#statusChange(BinderStatusChangeEvent)
 * @see BasicBinder#addStatusChangeListener(BinderStatusChangeListener)
 */
@SuppressWarnings("serial")
public class BinderStatusChangeEvent extends EventObject {

    private final boolean hasValidationErrors;
    private final boolean hasConversionErrors;

    /**
     * Create a new status change event for given {@code binder}, storing
     * information of whether the change that triggered this event caused
     * validation errors.
     *
     * @param binder
     *            the event source binder
     * @param hasValidationErrors
     *            the validation status associated with this event
     */
    public BinderStatusChangeEvent(BasicBinder<?> binder, boolean hasConversionErrors, boolean hasValidationErrors) {
        super(binder);
        this.hasConversionErrors = hasConversionErrors;
        this.hasValidationErrors = hasValidationErrors;
    }

    public boolean hasConversionErrors() {
        return hasConversionErrors;
    }

    /**
     * Gets the associated validation status.
     *
     * @return {@code true} if the change that triggered this event caused
     *         validation errors, {@code false} otherwise
     */
    public boolean hasValidationErrors() {
        return hasValidationErrors;
    }

    public boolean hasErrors() {
    	return hasValidationErrors() || hasConversionErrors();
    }

    @Override
    public BasicBinder<?> getSource() {
        return (BasicBinder<?>) super.getSource();
    }

    /**
     * Gets the binder.
     *
     * @return the binder
     */
    public BasicBinder<?> getBinder() {
        return getSource();
    }
}
