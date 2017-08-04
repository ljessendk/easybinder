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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.constraints.Min;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.data.BeanPropertySet;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.PropertyDefinition;
import com.vaadin.data.PropertySet;
import com.vaadin.data.RequiredFieldConfigurator;
import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;

public class ReflectionBinder<BEAN> extends BasicBinder<BEAN> {
	protected Class<BEAN> clazz;

	protected PropertySet<BEAN> propertySet;

	protected Map<String, EasyBinding<BEAN, ?, ?>> boundProperties = new HashMap<String, EasyBinding<BEAN, ?, ?>>();

	protected static ConverterRegistry globalConverterRegistry = ConverterRegistry.getInstance();

	private static RequiredFieldConfigurator MIN = annotation -> annotation.annotationType().equals(Min.class)
			&& ((Min) annotation).value() > 0;

	protected Logger log = Logger.getLogger(getClass().getName());

	protected RequiredFieldConfigurator requiredConfigurator = MIN.chain(RequiredFieldConfigurator.DEFAULT);

	public ReflectionBinder(Class<BEAN> clazz) {
		this.clazz = clazz;
		propertySet = BeanPropertySet.get(clazz);
	}

	public <PRESENTATION, MODEL> EasyBinding<BEAN, PRESENTATION, MODEL> bind(HasValue<PRESENTATION> field,
			String propertyName) {

		Objects.requireNonNull(propertyName, "Property name cannot be null");
		// checkUnbound();

		PropertyDefinition<BEAN, ?> definition = propertySet.getProperty(propertyName)
				.orElseThrow(() -> new IllegalArgumentException(
						"Could not resolve property name " + propertyName + " from " + propertySet));

		Optional<Class<PRESENTATION>> fieldTypeClass = getFieldTypeForField(field);

		Class<?> modelTypeClass = definition.getType();

		// Hack as PropertyDefinition does not return primitive type
		Optional<Field> modelField = getDeclaredFieldByName(definition.getPropertyHolderType(), definition.getName());
		if (modelField.isPresent()) {
			modelTypeClass = modelField.get().getType();
		}

		Converter<PRESENTATION, ?> converter;
		if (fieldTypeClass.isPresent()) {
			converter = globalConverterRegistry.getConverter(fieldTypeClass.get(), modelTypeClass);
			if (converter != null) {
				log.log(Level.INFO, "Converter for {0}->{1} found by lookup",
						new Object[] { fieldTypeClass.get(), modelTypeClass });
			}
			if (converter == null && fieldTypeClass.get().equals(modelTypeClass)) {
				if (modelTypeClass.isPrimitive()) {
					converter = Converter.identity();
					log.log(Level.INFO, "Converter for primitive {0}->{1} found by identity",
							new Object[] { fieldTypeClass.get(), modelTypeClass });
				} else {
					converter = new NullConverter<PRESENTATION>(field.getEmptyValue());
					log.log(Level.INFO, "Converter for non-primitive {0}->{1} found by identity",
							new Object[] { fieldTypeClass.get(), modelTypeClass });
				}
			}
		} else {
			converter = createConverter(modelTypeClass);
			log.log(Level.WARNING, "Converter for {0} generated", new Object[] { modelTypeClass });
		}

		return bind(field, propertyName, converter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <PRESENTATION, MODEL> EasyBinding<BEAN, PRESENTATION, MODEL> bind(HasValue<PRESENTATION> field,
			String propertyName, Converter<PRESENTATION, ?> converter) {
		Objects.requireNonNull(propertyName, "Property name cannot be null");
		// checkUnbound();

		PropertyDefinition<BEAN, ?> definition = propertySet.getProperty(propertyName)
				.orElseThrow(() -> new IllegalArgumentException(
						"Could not resolve property name " + propertyName + " from " + propertySet));

		ValueProvider<BEAN, ?> getter = definition.getGetter();
		Setter<BEAN, ?> setter = definition.getSetter().orElse((bean, value) -> {
			// Setter ignores value
		});

		EasyBinding<BEAN, PRESENTATION, MODEL> binding = bind(field, (ValueProvider) getter, (Setter) setter,
				propertyName, (Converter) converter);

		boundProperties.put(propertyName, binding);

		Optional<Field> modelField = getDeclaredFieldByName(definition.getPropertyHolderType(), definition.getName());
		if (modelField.isPresent()) {
			if (Arrays.asList(modelField.get().getAnnotations()).stream().anyMatch(requiredConfigurator)) {
				field.setRequiredIndicatorVisible(true);
			}
		}

		return binding;
	}

	@SuppressWarnings("unchecked")
	protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> getConverter(Optional<Class<PRESENTATION>> fieldType,
			Class<MODEL> propertyType) {
		if (fieldType.isPresent()) {
			Converter<PRESENTATION, MODEL> converter = globalConverterRegistry.getConverter(fieldType.get(),
					propertyType);
			if (converter != null) {
				return converter;
			} else if (fieldType.get().equals(propertyType)) {
				return (Converter<PRESENTATION, MODEL>) Converter.identity();
			}
		}

		return createConverter(propertyType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> createConverter(Class<MODEL> propertyType) {
		return (Converter) Converter.from(fieldValue -> propertyType.cast(fieldValue),
				propertyValue -> (MODEL) propertyValue, exception -> {
					throw new RuntimeException(exception);
				});
	}

	@SuppressWarnings("unchecked")
	protected <PRESENTATION> Optional<Class<PRESENTATION>> getFieldTypeForField(HasValue<PRESENTATION> field) {
		// Try to find the field type using reflection
		Type valueType = GenericTypeReflector.getTypeParameter(field.getClass(), HasValue.class.getTypeParameters()[0]);

		return Optional.ofNullable((Class<PRESENTATION>) valueType);
	}

	protected Optional<Field> getDeclaredFieldByName(Class<?> searchClass, String name) {
		while (searchClass != null) {
			try {
				return Optional.of(searchClass.getDeclaredField(name));
			} catch (NoSuchFieldException | SecurityException e) {
				// No such field, try superclass
				searchClass = searchClass.getSuperclass();
			}
		}
		return Optional.empty();
	}

	/**
	 * Sets a logic which allows to configure require indicator via
	 * {@link HasValue#setRequiredIndicatorVisible(boolean)} based on property
	 * descriptor.
	 * <p>
	 * Required indicator configuration will not be used at all if
	 * {@code configurator} is null.
	 * <p>
	 * By default the {@link RequiredFieldConfigurator#DEFAULT} configurator is
	 * used.
	 *
	 * @param configurator
	 *            required indicator configurator, may be {@code null}
	 */
	public void setRequiredConfigurator(RequiredFieldConfigurator configurator) {
		requiredConfigurator = configurator;
	}

	/**
	 * Gets field required indicator configuration logic.
	 *
	 * @see #setRequiredConfigurator(RequiredFieldConfigurator)
	 *
	 * @return required indicator configurator, may be {@code null}
	 */
	public RequiredFieldConfigurator getRequiredConfigurator() {
		return requiredConfigurator;
	}

}
