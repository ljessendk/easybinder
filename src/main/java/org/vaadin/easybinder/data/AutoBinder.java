/*
 * Copyright 2017 Lars Sønderby Jessen
 *
 * Partly based on code copied from Vaadin Framework:
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.annotations.PropertyId;
import com.vaadin.data.HasValue;
import com.vaadin.data.PropertyDefinition;
import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;

public class AutoBinder<BEAN> extends ReflectionBinder<BEAN> {

	public AutoBinder(Class<BEAN> clazz) {
		super(clazz);
	}

	/**
	 * Binds member fields found in the given object.
	 * <p>
	 * This method processes all (Java) member fields whose type extends
	 * {@link HasValue} and that can be mapped to a property id. Property name
	 * mapping is done based on the field name or on a @{@link PropertyId}
	 * annotation on the field. All non-null unbound fields for which a property
	 * name can be determined are bound to the property name using
	 * {@link ReflectionBinder#bind(HasValue, String)}.
	 * <p>
	 * For example:
	 *
	 * <pre>
	 * public class MyForm extends VerticalLayout {
	 * private TextField firstName = new TextField("First name");
	 * &#64;PropertyId("last")
	 * private TextField lastName = new TextField("Last name");
	 *
	 * MyForm myForm = new MyForm();
	 * ...
	 * binder.bindMemberFields(myForm);
	 * </pre>
	 *
	 * This binds the firstName TextField to a "firstName" property in the item,
	 * lastName TextField to a "last" property.
	 * <p>
	 * It's always possible to do custom binding for any field: the
	 * {@link #bindInstanceFields(Object)} method doesn't override existing
	 * bindings.
	 *
	 * @param objectWithMemberFields
	 *            The object that contains (Java) member fields to bind
	 * @throws IllegalStateException
	 *             if there are incompatible HasValue&lt;T&gt; and property types
	 */
	public void bindInstanceFields(Object objectWithMemberFields) {
		Class<?> objectClass = objectWithMemberFields.getClass();

		Integer numberOfBoundFields = getFieldsInDeclareOrder(objectClass).stream()
				.filter(memberField -> HasValue.class.isAssignableFrom(memberField.getType()))
				.filter(memberField -> !isFieldBound(memberField, objectWithMemberFields))
				.map(memberField -> handleProperty(memberField, objectWithMemberFields,
						(property, type) -> bindProperty(objectWithMemberFields, memberField, property, type)))
				.reduce(0, this::accumulate, Integer::sum);
		if (numberOfBoundFields == 0 && bindings.isEmpty()) {
			// Throwing here for incomplete bindings would be wrong as they
			// may be completed after this call. If they are not, setBean and
			// other methods will throw for those cases
			throw new IllegalStateException("There are no instance fields " + "found for automatic binding");
		}

	}

	/**
	 * Binds {@code property} with {@code propertyType} to the field in the
	 * {@code objectWithMemberFields} instance using {@code memberField} as a
	 * reference to a member.
	 *
	 * @param objectWithMemberFields
	 *            the object that contains (Java) member fields to build and bind
	 * @param memberField
	 *            reference to a member field to bind
	 * @param propertyName
	 *            property name to bind
	 * @param propertyType
	 *            type of the property
	 * @return {@code true} if property is successfully bound
	 */
	protected boolean bindProperty(Object objectWithMemberFields, Field memberField, String propertyName,
			Class<?> propertyType) {
		log.log(Level.INFO, "Binding property, field={0}, property={1}",
				new Object[] { memberField.getName(), propertyName });
		Type valueType = GenericTypeReflector.getTypeParameter(memberField.getGenericType(),
				HasValue.class.getTypeParameters()[0]);
		if (valueType == null) {
			throw new IllegalStateException(
					String.format("Unable to detect value type for the member '%s' in the " + "class '%s'.",
							memberField.getName(), objectWithMemberFields.getClass().getName()));
		}

		HasValue<?> field;
		// Get the field from the object
		try {
			field = (HasValue<?>) ReflectTools.getJavaFieldValue(objectWithMemberFields, memberField, HasValue.class);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			log.log(Level.INFO, "Not able to determine type of field");
			// If we cannot determine the value, just skip the field
			return false;
		}

		bind(field, propertyName);
		return true;

	}

	/**
	 * Returns an array containing {@link Field} objects reflecting all the fields
	 * of the class or interface represented by this Class object. The elements in
	 * the array returned are sorted in declare order from sub class to super class.
	 *
	 * @param searchClass
	 *            class to introspect
	 * @return list of all fields in the class considering hierarchy
	 */
	protected List<Field> getFieldsInDeclareOrder(Class<?> searchClass) {
		ArrayList<Field> memberFieldInOrder = new ArrayList<>();

		while (searchClass != null) {
			memberFieldInOrder.addAll(Arrays.asList(searchClass.getDeclaredFields()));
			searchClass = searchClass.getSuperclass();
		}
		return memberFieldInOrder;
	}

	protected boolean isFieldBound(Field memberField, Object objectWithMemberFields) {
		try {
			HasValue<?> field = (HasValue<?>) getMemberFieldValue(memberField, objectWithMemberFields);
			return bindings.stream().anyMatch(binding -> binding.getField() == field);
		} catch (Exception e) {
			return false;
		}
	}

	protected Object getMemberFieldValue(Field memberField, Object objectWithMemberFields) {
		memberField.setAccessible(true);
		try {
			return memberField.get(objectWithMemberFields);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			memberField.setAccessible(false);
		}
	}

	protected boolean handleProperty(Field field, Object objectWithMemberFields,
			BiFunction<String, Class<?>, Boolean> propertyHandler) {
		Optional<PropertyDefinition<BEAN, ?>> descriptor = getPropertyDescriptor(field);

		if (!descriptor.isPresent()) {
			log.log(Level.INFO, "No property descriptor found for field={0}", field.getName());
			return false;
		}

		PropertyId propertyIdAnnotation = field.getAnnotation(PropertyId.class);
		String propertyName;
		if (propertyIdAnnotation != null) {
			// @PropertyId(propertyId) always overrides property id
			propertyName = propertyIdAnnotation.value();
		} else {
			propertyName = field.getName();
		}

		if (boundProperties.containsKey(propertyName)) {
			return false;
		}

		Boolean isPropertyBound = propertyHandler.apply(propertyName, descriptor.get().getType());
		assert boundProperties.containsKey(propertyName);
		return isPropertyBound;
	}

	protected int accumulate(int count, boolean value) {
		return value ? count + 1 : count;
	}

	protected Optional<PropertyDefinition<BEAN, ?>> getPropertyDescriptor(Field field) {
		PropertyId propertyIdAnnotation = field.getAnnotation(PropertyId.class);

		String propertyId;
		if (propertyIdAnnotation != null) {
			// @PropertyId(propertyId) always overrides property id
			propertyId = propertyIdAnnotation.value();
		} else {
			propertyId = field.getName();
		}

		return propertySet.getProperty(propertyId);
	}

	protected Component createAndBind(Field f, String path) {
		Optional<Component> c = ComponentFactoryRegistry.getInstance().createComponent(f);
		if (!c.isPresent()) {
			throw new RuntimeException("No Component factory matches field, field=<" + f + ">");
		}

		if (c.get() instanceof HasValue<?>) {
			HasValue<?> h = (HasValue<?>) c.get();
			bind(h, path + f.getName());
		}
		return c.get();
	}

	protected <T> void buildAndBind(Class<?> currentClazz, String path, List<Component> components,
			String... nestedProperties) {
		List<Field> fields = getFieldsInDeclareOrder(currentClazz);

		Map<String, List<String>> nestedPropertyMap = new HashMap<>();

		for (String p : nestedProperties) {
			int index = p.indexOf('.', 0);
			String current = index == -1 ? p : p.substring(0, index);
			List<String> next = nestedPropertyMap.get(current);
			if (next == null) {
				next = new LinkedList<>();
				nestedPropertyMap.put(current, next);
			}
			if (index != -1) {
				next.add(p.substring(index + 1, p.length()));
			}
		}

		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				continue;
			}
			if (boundProperties.containsKey(path + field.getName())) {
				// property already bound, skip
				continue;
			}
			if (nestedPropertyMap.containsKey(field.getName())) {
				buildAndBind(field.getType(), path + field.getName() + ".", components,
						nestedPropertyMap.get(field.getName()).stream().toArray(String[]::new));
			} else {
				components.add(createAndBind(field, path));
			}
		}
	}

	/**
	 * Constructs UI form components based on properties in a POJO class and binds
	 * the components to the properties.
	 * <p>
	 * This method processes all non-static (Java) member fields in a class and
	 * tries to construct an UI component by ... TODO This method processes all
	 * (Java) member fields whose type extends {@link HasValue} and that can be
	 * mapped to a property id. Property name mapping is done based on the field
	 * name or on a @{@link PropertyId} annotation on the field. All non-null
	 * unbound fields for which a property name can be determined are bound to the
	 * property name using {@link ReflectionBinder#bind(HasValue, String)}.
	 * <p>
	 * For example:
	 *
	 * <pre>
	 * formLayout.addComponents(binder.buildAndBind("car", "car.engine"));
	 * </pre>
	 *
	 * This iterates over all properties in the class and tries to construct UI
	 * components based on the property. The "car" and "car.engine" will be handled
	 * as nested properties.
	 *
	 * @param nestedProperties
	 *            List of nested properies that should be expanded.
	 * @return list of components build by the process
	 */
	public Component[] buildAndBind(String... nestedProperties) {
		List<Component> components = new LinkedList<>();
		buildAndBind(clazz, "", components, nestedProperties);
		return components.stream().toArray(Component[]::new);
	}

	protected <T> void buildAndBind(Class<?> currentClazz, String path, List<Component> components,
			Set<Class<?>> nestedClasses) {
		List<Field> fields = getFieldsInDeclareOrder(currentClazz);

		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				continue;
			}
			if (boundProperties.containsKey(path + field.getName())) {
				// property already bound, skip
				continue;
			}
			if (nestedClasses.contains(field.getType())) {
				buildAndBind(field.getType(), path + field.getName() + ".", components,
						nestedClasses);
			} else {
				components.add(createAndBind(field, path));
			}
		}
	}

	public Component[] buildAndBind(Set<Class<?>> nestedClasses) {
		List<Component> components = new LinkedList<>();
		buildAndBind(clazz, "", components, nestedClasses);
		return components.stream().toArray(Component[]::new);
	}

}
