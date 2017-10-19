/*
 * Copyright 2017 Lars Sønderby Jessen
 *
 * Partly based on code copied from Vaadin Framework (Binder)
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.vaadin.data.Binder.Binding;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.BindingValidationStatus.Status;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.Result;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.EventRouter;
import com.vaadin.server.Setter;
import com.vaadin.server.UserError;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class BasicBinder<BEAN> {

	public static class EasyBinding<BEAN, FIELDVALUE, TARGET> implements Binding<BEAN, FIELDVALUE> {
		protected final HasValue<FIELDVALUE> field;
		protected final ValueProvider<BEAN, TARGET> getter;
		protected final Setter<BEAN, TARGET> setter;
		protected final Optional<String> property;

		protected final Converter<FIELDVALUE, TARGET> converterValidatorChain;
		protected Registration registration;

		protected Optional<String> conversionError = Optional.empty();
		protected Optional<String> validationError = Optional.empty();

		public EasyBinding(BasicBinder<BEAN> binder, HasValue<FIELDVALUE> field, ValueProvider<BEAN, TARGET> getter,
				Setter<BEAN, TARGET> setter, Optional<String> property,
				Converter<FIELDVALUE, TARGET> converterValidatorChain) {
			this.field = field;
			this.getter = getter;
			this.setter = setter;
			this.property = property;
			this.converterValidatorChain = converterValidatorChain;

			registration = field.addValueChangeListener(e -> {
				if (binder.getBean() != null) {
					if (binder.fieldToBean(this)) {
						binder.fireValueChangeEvent(e);
					}
				}
			});

			if (setter == null) {
				field.setReadOnly(true);
			}
		}

		public void setReadOnly(boolean readOnly) {
			field.setReadOnly(setter == null || readOnly);
		}

		public void remove() {
			registration.remove();
		}

		public void beanToField(BEAN bean) {
			field.setValue(converterValidatorChain.convertToPresentation(getter.apply(bean), createValueContext()));
		}

		public boolean fieldToBean(BEAN bean) {
			if (setter == null || field.isReadOnly()) {
				return false;
			}
			Result<TARGET> result = converterValidatorChain.convertToModel(field.getValue(), createValueContext());
			result.ifError(e -> setConversionError(e));
			result.ifOk(e -> {
				clearConversionError();
				if (setter != null) {
					setter.accept(bean, e);
				}
			});
			return !result.isError();
		}

		@Override
		public HasValue<FIELDVALUE> getField() {
			return field;
		}

		/**
		 * Creates a value context from the current state of the binding and its field.
		 *
		 * @return the value context
		 */
		protected ValueContext createValueContext() {
			if (field instanceof Component) {
				return new ValueContext((Component) field, field);
			}
			return new ValueContext(null, field, findLocale());
		}

		/**
		 * Finds an appropriate locale to be used in conversion and validation.
		 *
		 * @return the found locale, not null
		 */
		protected Locale findLocale() {
			Locale l = null;
			if (field instanceof Component) {
				l = ((Component) field).getLocale();
			}
			if (l == null && UI.getCurrent() != null) {
				l = UI.getCurrent().getLocale();
			}
			if (l == null) {
				l = Locale.getDefault();
			}
			return l;
		}

		public Optional<String> getProperty() {
			return property;
		}

		public boolean hasValidationError() {
			return validationError.isPresent();
		}

		public boolean hasConversionError() {
			return conversionError.isPresent();
		}

		public boolean hasError() {
			return hasValidationError() || hasConversionError();
		}

		@Override
		public BindingValidationStatus<FIELDVALUE> validate() {
			return new BindingValidationStatus<FIELDVALUE>(this, hasError() ? Status.ERROR : Status.OK,
					conversionError.isPresent() ? ValidationResult.error(conversionError.get())
							: validationError.isPresent() ? ValidationResult.error(validationError.get())
									: ValidationResult.ok());
		}

		protected void setConversionError(String errorMessage) {
			Objects.requireNonNull(errorMessage);
			conversionError = Optional.of(errorMessage);
		}

		protected void clearConversionError() {
			conversionError = Optional.empty();
		}

		public Optional<String> getConversionError() {
			return conversionError;
		}

		public void setValidationError(String errorMessage) {
			Objects.requireNonNull(errorMessage);
			validationError = Optional.of(errorMessage);
		}

		public void clearValidationError() {
			validationError = Optional.empty();
		}

		public Optional<String> getValidationError() {
			return validationError;
		}

		public Optional<String> getError() {
			if (conversionError.isPresent()) {
				return conversionError;
			} else {
				return validationError;
			}
		}

		public ValueProvider<BEAN, TARGET> getGetter() {
			return getter;
		}

	}

	protected BEAN bean;

	protected Label statusLabel;

	protected List<EasyBinding<BEAN, ?, ?>> bindings = new LinkedList<>();
	protected Map<String, EasyBinding<BEAN, ?, ?>> propertyToBindingMap = new HashMap<>();

	protected Set<ConstraintViolation<BEAN>> constraintViolations;

	protected boolean hasChanges = false;

	protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	protected Class<?>[] groups = new Class<?>[0];

	protected EventRouter eventRouter;

	public BasicBinder() {
		validate();
	}

	public void setBean(BEAN bean) {
		this.bean = null;

		if (bean != null) {
			bindings.forEach(e -> e.beanToField(bean));
		}

		this.bean = bean;
		validate();
		bindings.forEach(e -> handleError(e.getField(), e.getError()));
		fireStatusChangeEvent();
		hasChanges = false;
	}

	public BEAN getBean() {
		return bean;
	}

	public void removeBean() {
		setBean(null);
	}

	public void setValidationGroups(Class<?>... groups) {
		this.groups = groups;
		validate();
	}

	public Class<?>[] getValidationGroups() {
		return groups;
	}

	public void clearValidationGroups() {
		groups = new Class<?>[0];
		validate();
	}

	public boolean isValid() {
		return constraintViolations.isEmpty();
	}

	public <FIELDVALUE, TARGET> EasyBinding<BEAN, FIELDVALUE, ?> bind(HasValue<FIELDVALUE> field,
			ValueProvider<BEAN, FIELDVALUE> getter, Setter<BEAN, FIELDVALUE> setter, String property) {
		return bind(field, getter, setter, property, Converter.identity());
	}

	public <FIELDVALUE, TARGET> EasyBinding<BEAN, FIELDVALUE, TARGET> bind(HasValue<FIELDVALUE> field,
			ValueProvider<BEAN, TARGET> getter, Setter<BEAN, TARGET> setter, String property,
			Converter<FIELDVALUE, TARGET> converter) {

		Objects.requireNonNull(field);
		Objects.requireNonNull(getter);
		Objects.requireNonNull(converter);

		// Register as binding
		EasyBinding<BEAN, FIELDVALUE, TARGET> binding = new EasyBinding<BEAN, FIELDVALUE, TARGET>(this, field, getter,
				setter, Optional.ofNullable(property), converter);

		// TODO: remove from binding
		/*
		 * binding.registration = field.addValueChangeListener(e -> { if (getBean() !=
		 * null) { if(fieldToBean(binding)) { fireValueChangeEvent(e); } } });
		 */

		bindings.add(binding);

		// Add property to validation error map
		if (property != null) {
			propertyToBindingMap.put(property, binding);
		}

		if (getBean() != null) {
			if (fieldToBean(binding)) {
				// TODO: should this be fired?
				// fireValueChangeEvent(e);
			}
		} else {
			fireStatusChangeEvent();
		}

		return binding;
	}

	public void unbind() {
		while (!bindings.isEmpty()) {
			EasyBinding<BEAN, ?, ?> binding = bindings.remove(0);
			binding.getProperty().ifPresent(e -> propertyToBindingMap.remove(e));
			binding.remove();
		}
	}

	public void removeBinding(HasValue<?> field) {
		bindings.stream().filter(e -> e.getField().equals(field)).findFirst().ifPresent(e -> clearBinding(e));
		validate();
	}

	public <FIELDVALUE, TARGET> void removeBinding(EasyBinding<BEAN, FIELDVALUE, TARGET> binding) {
		clearBinding(binding);
		validate();
	}

	public void removeBinding(String propertyValue) {
		Objects.requireNonNull(propertyValue);
		Optional.ofNullable(propertyToBindingMap.get(propertyValue)).ifPresent(e -> removeBinding(e));
		validate();
	}

	protected <FIELDVALUE, TARGET> void clearBinding(EasyBinding<BEAN, FIELDVALUE, TARGET> binding) {
		if (bindings.remove(binding)) {
			binding.remove();
		}
		binding.getProperty().ifPresent(e -> propertyToBindingMap.remove(e));
	}

	public Stream<HasValue<?>> getFields() {
		return bindings.stream().map(e -> e.getField());
	}

	protected void handleConstraintViolations(ConstraintViolation<BEAN> v,
			Function<ConstraintViolation<BEAN>, String> f) {
		String property = v.getPropertyPath().toString();
		if (property.isEmpty()) {
			// Bean level validation error
			if (statusLabel != null) {
				statusLabel.setValue(f.apply(v));
			}
		} else {
			// Field validation error
			Optional.ofNullable(propertyToBindingMap.get(property)).ifPresent(e -> e.setValidationError(f.apply(v)));
		}
	}

	protected void validate() {
		// Clear validation errors
		getStatusLabel().ifPresent(e -> e.setValue(""));

		// Clear all validation errors
		propertyToBindingMap.values().stream().forEach(e -> e.clearValidationError());

		// Validate and set validation errors
		if (getBean() != null) {
			constraintViolations = validator.validate(getBean(), groups);
			constraintViolations.stream().forEach(e -> handleConstraintViolations(e, f -> f.getMessage()));
			// Handle errors
			propertyToBindingMap.values().stream().forEach(e -> handleError(e.getField(), e.getError()));
		} else {
			constraintViolations = new HashSet<ConstraintViolation<BEAN>>();
		}
	}

	/**
	 * Clears the error condition of the given field, if any. The default
	 * implementation clears the
	 * {@link AbstractComponent#setComponentError(ErrorMessage) component error} of
	 * the field if it is a Component, otherwise does nothing.
	 *
	 * @param field
	 *            the field with an invalid value
	 */
	protected void clearError(HasValue<?> field) {
		if (field instanceof AbstractComponent) {
			((AbstractComponent) field).setComponentError(null);
		}
	}

	/**
	 * Gets the status label or an empty optional if none has been set.
	 *
	 * @return the optional status label
	 * @see #setStatusLabel(Label)
	 */
	public Optional<Label> getStatusLabel() {
		return Optional.ofNullable(statusLabel);
	}

	protected void handleError(HasValue<?> field, Optional<String> error) {
		if (error.isPresent()) {
			setError(field, error.get());
		} else {
			clearError(field);
		}
	}

	/**
	 * Handles a validation error emitted when trying to write the value of the
	 * given field. The default implementation sets the
	 * {@link AbstractComponent#setComponentError(ErrorMessage) component error} of
	 * the field if it is a Component, otherwise does nothing.
	 *
	 * @param field
	 *            the field with the invalid value
	 * @param error
	 *            the error message to set
	 */
	protected void setError(HasValue<?> field, String error) {
		if (field instanceof AbstractComponent) {
			((AbstractComponent) field).setComponentError(new UserError(error));
		}
	}

	public void setStatusLabel(Label statusLabel) {
		this.statusLabel = statusLabel;
	}

	public Optional<HasValue<?>> getFieldForProperty(String propertyName) {
		return Optional.ofNullable(propertyToBindingMap.get(propertyName)).map(e -> e.getField());
	}

	/**
	 * Adds field value change listener to all the fields in the binder.
	 * <p>
	 * Added listener is notified every time whenever any bound field value is
	 * changed. The same functionality can be achieved by adding a
	 * {@link ValueChangeListener} to all fields in the {@link Binder}.
	 * <p>
	 * The listener is added to all fields regardless of whether the method is
	 * invoked before or after field is bound.
	 *
	 * @see ValueChangeEvent
	 * @see ValueChangeListener
	 *
	 * @param listener
	 *            a field value change listener
	 * @return a registration for the listener
	 */
	public Registration addValueChangeListener(ValueChangeListener<?> listener) {
		return getEventRouter().addListener(ValueChangeEvent.class, listener,
				ValueChangeListener.class.getDeclaredMethods()[0]);
	}

	/**
	 * Returns the event router for this binder.
	 *
	 * @return the event router, not null
	 */
	protected EventRouter getEventRouter() {
		if (eventRouter == null) {
			eventRouter = new EventRouter();
		}
		return eventRouter;
	}

	/**
	 * Adds status change listener to the binder.
	 * <p>
	 * The {@link Binder} status is changed whenever any of the following happens:
	 * <ul>
	 * <li>if it's bound and any of its bound field or select has been changed
	 * <li>{@link #setBean(Object)} is called
	 * <li>{@link #removeBean()} is called
	 * <li>{@link #bind(HasValue, ValueProvider, Setter, String)} is called
	 * </ul>
	 *
	 * @see #setBean(Object)
	 * @see #removeBean()
	 *
	 * @param listener
	 *            status change listener to add, not null
	 * @return a registration for the listener
	 */
	public Registration addStatusChangeListener(BinderStatusChangeListener listener) {
		return getEventRouter().addListener(BinderStatusChangeEvent.class, listener,
				BinderStatusChangeListener.class.getDeclaredMethods()[0]);
	}

	public boolean getHasChanges() {
		return hasChanges;
	}

	protected <V> void fireValueChangeEvent(ValueChangeEvent<V> event) {
		hasChanges = true;
		getEventRouter().fireEvent(event);
	}

	protected void fireStatusChangeEvent() {
		boolean hasConversionErrors = bindings.stream().anyMatch(e -> e.hasConversionError());
		getEventRouter()
				.fireEvent(new BinderStatusChangeEvent(this, hasConversionErrors, !constraintViolations.isEmpty()));
	}

	public Optional<EasyBinding<BEAN, ?, ?>> getBinding(String propertyName) {
		Objects.requireNonNull(propertyName);
		return Optional.ofNullable(propertyToBindingMap.get(propertyName));
	}

	public void setReadonly(boolean readOnly) {
		bindings.stream().forEach(e -> e.setReadOnly(readOnly));
	}

	protected boolean fieldToBean(EasyBinding<BEAN, ?, ?> binding) {
		Optional<String> currentError = binding.getError();

		boolean conversionOk = binding.fieldToBean(getBean());
		if (conversionOk) {
			Optional<String> currentValidationError = binding.getValidationError();
			validate();
			if (!currentValidationError.equals(binding.getValidationError())) {
				// TODO: only fire if global change
				fireStatusChangeEvent();
			}
		}

		if (!currentError.equals(binding.getError())) {
			handleError(binding.getField(), binding.getError());
		}
		fireStatusChangeEvent();

		return conversionOk;
	}

	public List<EasyBinding<BEAN, ?, ?>> getBindings() {
		return Collections.unmodifiableList(bindings);
	}

	public Set<ConstraintViolation<BEAN>> getConstraintViolations() {
		return constraintViolations;
	}

}
