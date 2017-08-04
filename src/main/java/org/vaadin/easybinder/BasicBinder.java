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
package org.vaadin.easybinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
//import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.ValueProvider;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Setter;
import com.vaadin.server.UserError;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

public class BasicBinder<BEAN> {

	public static class EasyBinding<BEAN, FIELDVALUE, TARGET> {
		protected final HasValue<FIELDVALUE> field;
		protected final ValueProvider<BEAN, TARGET> getter;
		protected final Setter<BEAN, TARGET> setter;
		protected final String property;

		protected final Converter<FIELDVALUE, TARGET> converterValidatorChain;
		protected final BasicBinder<BEAN> binder;
		protected final Registration registration;

		public EasyBinding(BasicBinder<BEAN> binder, HasValue<FIELDVALUE> field, ValueProvider<BEAN, TARGET> getter,
				Setter<BEAN, TARGET> setter, String property, Converter<FIELDVALUE, TARGET> converterValidatorChain) {
			this.binder = binder;
			this.field = field;
			this.getter = getter;
			this.setter = setter;
			this.property = property;
			this.converterValidatorChain = converterValidatorChain;

			registration = field.addValueChangeListener(e -> {
				if (binder.getBean() != null) {
					fieldToBean(binder.getBean());
					// binder.validate();
				}
			});
		}

		public void remove() {
			registration.remove();
		}

		public void beanToField(BEAN bean) {
			field.setValue(converterValidatorChain.convertToPresentation(getter.apply(bean), createValueContext()));
		}

		public void fieldToBean(BEAN bean) {
			Result<TARGET> result = converterValidatorChain.convertToModel(field.getValue(), createValueContext());
			result.ifError(e -> binder.setConversionError(field, e));
			result.ifOk(e -> {
				binder.clearConversionError(field);
				setter.accept(bean, e);
				binder.validate();
			});
		}

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

		public String getProperty() {
			return property;
		}

	}

	protected BEAN bean;

	protected Label statusLabel;

	protected Map<String, HasValue<?>> validationErrorMap = new HashMap<String, HasValue<?>>();

	protected List<EasyBinding<BEAN, ?, ?>> bindings = new LinkedList<EasyBinding<BEAN, ?, ?>>();

	protected Set<ConstraintViolation<BEAN>> constraintViolations;
	protected Map<HasValue<?>, String> conversionViolations = new HashMap<>();

	protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	protected Class<?>[] groups = new Class<?>[0];

	public BasicBinder() {
		validate();
	}

	public void setBean(BEAN bean) {
		this.bean = bean;

		bindings.forEach(e -> e.beanToField(bean));
		validate();
	}

	public BEAN getBean() {
		return bean;
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
		// Register as binding
		EasyBinding<BEAN, FIELDVALUE, TARGET> binding = new EasyBinding<BEAN, FIELDVALUE, TARGET>(this, field, getter,
				setter, property, converter);
		bindings.add(binding);

		// Add property to validation error map
		if (property != null) {
			validationErrorMap.put(property, field);
		}

		if (getBean() != null) {
			binding.fieldToBean(getBean());
			// validate();
		}

		return binding;
	}

	public <FIELDVALUE, TARGET> void unbind(EasyBinding<BEAN, FIELDVALUE, TARGET> binding) {
		if (bindings.remove(binding)) {
			binding.remove();
		}

		if (binding.getProperty() != null) {
			validationErrorMap.remove(binding.getProperty());
		}

		validate();
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
			HasValue<?> g = validationErrorMap.get(property);
			if (g != null) {
				handleError(g, f.apply(v));
			}
		}
	}

	protected void validate() {
		// Clear validation errors
		validationErrorMap.values().stream().forEach(e -> clearError(e));

		// Validate and set validation errors
		if (getBean() != null) {
			constraintViolations = validator.validate(getBean(), groups);
			constraintViolations.stream().forEach(e -> handleConstraintViolations(e, f -> f.getMessage()));
		} else {
			constraintViolations = new HashSet<ConstraintViolation<BEAN>>();
		}

		conversionViolations.entrySet().stream().forEach(e -> handleError(e.getKey(), e.getValue()));

	}

	protected void setConversionError(HasValue<?> field, String message) {
		conversionViolations.put(field, message);
		handleError(field, message);
	}

	protected void clearConversionError(HasValue<?> field) {
		conversionViolations.remove(field);
		clearError(field);
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
	protected void handleError(HasValue<?> field, String error) {
		if (field instanceof AbstractComponent) {
			((AbstractComponent) field).setComponentError(new UserError(error));
		}
	}

	public void setStatusLabel(Label statusLabel) {
		this.statusLabel = statusLabel;
	}

	public Optional<HasValue<?>> getFieldForProperty(String propertyName) {
		return Optional.ofNullable(validationErrorMap.get(propertyName));
	}
}
