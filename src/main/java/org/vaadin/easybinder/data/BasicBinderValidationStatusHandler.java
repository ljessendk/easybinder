/*
 * Copyright 2017 Lars Sønderby Jessen
 *
 * Mostly based on code copied from Vaadin Framework (BinderValidationStatusHandler)
 * Copyright 2000-2016 Vaadin Ltd.
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

import com.vaadin.event.SerializableEventListener;

/**
 * Handler for {@link BasicBinderValidationStatus} changes.
 * <p>
 * {@link Binder#setValidationStatusHandler(BinderValidationStatusHandler)
 * Register} an instance of this class to be able to customize validation status
 * handling.
 * <p>
 * The default handler will show
 * {@link com.vaadin.ui.AbstractComponent#setComponentError(com.vaadin.server.ErrorMessage)
 * an error message} for failed field validations. For bean level validation
 * errors it will display the first error message in
 * {@link BasicBinder#setStatusLabel(com.vaadin.ui.Label) status label}, if one has
 * been set.
 *
 * @see BasicBinderValidationStatus
 * @see BasicBinder#validate()
 * @see BindingValidationStatus
 *
 * @param <BEAN>
 *            the bean type of binder
 */
@FunctionalInterface
public interface BasicBinderValidationStatusHandler<BEAN> extends SerializableEventListener {

	/**
	 * Invoked when the validation status has changed in binder.
	 *
	 * @param statusChange
	 *            the changed status
	 */
	void statusChange(BasicBinderValidationStatus<BEAN> statusChange);
}
