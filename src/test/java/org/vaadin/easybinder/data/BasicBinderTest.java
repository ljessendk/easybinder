package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.vaadin.easybinder.data.BasicBinder;
import org.vaadin.easybinder.data.BinderStatusChangeListener;
import org.vaadin.easybinder.data.BasicBinder.EasyBinding;
import org.vaadin.easybinder.data.converters.NullConverter;
import org.vaadin.easybinder.data.converters.StringLengthConverterValidator;
import org.vaadin.easybinder.usagetest.BasicBinderGroupingTest.MyEntity2;
import org.vaadin.easybinder.usagetest.BasicBinderGroupingTest.MyGroup;

import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class BasicBinderTest {

	public class MyEntity {
		@NotNull
		String firstName;
		String lastName;
		@Min(-10)
		int age;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

	BasicBinder<MyEntity> binder = new BasicBinder<>();
	TextField firstName = new TextField();
	TextField lastName = new TextField();
	TextField age = new TextField();

	@Test
	public void testBindFields() {
		assertEquals(0, binder.getBindings().size());
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		assertEquals(2, binder.getBindings().size());
		assertEquals(firstName, binder.getFieldForProperty("firstName").get());
		assertEquals(lastName, binder.getFieldForProperty("lastName").get());
	}

	@Test
	public void testBindNoSetter() {
		MyEntity bean = new MyEntity();
		@SuppressWarnings("unchecked")
		HasValue<String> field = mock(HasValue.class);
		EasyBinding<MyEntity, String, String> binding = binder.bind(field, MyEntity::getLastName, null, "firstName");
		assertFalse(binding.fieldToBean(bean));
	}

	@Test
	public void testBindReadOnly() {
		MyEntity bean = new MyEntity();
		@SuppressWarnings("unchecked")
		HasValue<String> field = mock(HasValue.class);
		when(field.isReadOnly()).thenReturn(true);
		EasyBinding<MyEntity, String, String> binding = binder.bind(field, MyEntity::getLastName, MyEntity::setLastName, "firstName");
		assertFalse(binding.fieldToBean(bean));
	}

	@Test
	public void testRemoveBindingByField() {
		assertEquals(0, binder.getBindings().size());
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		assertEquals(2, binder.getBindings().size());
		binder.removeBinding(firstName);
		assertEquals(1, binder.getBindings().size());
	}

	@Test
	public void testRemoveBindingByPropertyName() {
		assertEquals(0, binder.getBindings().size());
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		assertEquals(2, binder.getBindings().size());
		binder.removeBinding("firstName");
		assertEquals(1, binder.getBindings().size());
	}

	@Test
	public void testRemoveBindingByBinding() {
		assertEquals(0, binder.getBindings().size());
		EasyBinding<MyEntity, String, ?> binding = binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		assertEquals(2, binder.getBindings().size());
		binder.removeBinding(binding);
		assertEquals(1, binder.getBindings().size());
		binder.removeBinding(binding);
		assertEquals(1, binder.getBindings().size());
	}

	@Test
	public void testRemoveAllBindings() {
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		assertEquals(2, binder.getBindings().size());
		binder.removeAllBindings();
		assertEquals(0, binder.getBindings().size());
	}

	@Test
	public void testSetBeanBeforeBind() {
		MyEntity entity = new MyEntity();
		entity.setLastName("Doe");
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		binder.setBean(entity);

		assertFalse(binder.isValid());

		assertEquals("Doe", lastName.getValue());
	}

	@Test
	public void testNoPropertyProvided() {
		MyEntity entity = new MyEntity();
		binder.setBean(entity);
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), null, new NullConverter<>(""));
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, null, new NullConverter<>(""));

		assertFalse(binder.isValid());
	}

	@Test
	public void testGetBindingByBinding() {
		assertFalse(binder.getBinding("firstName").isPresent());
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		assertTrue(binder.getBinding("firstName").isPresent());
	}

	@Test
	public void testGetFields() {
		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), null, new NullConverter<>(""));
		assertEquals(1, binder.getFields().collect(Collectors.toList()).size());
		assertEquals(firstName, binder.getFields().findAny().get());
	}

	@Test
	public void testValidateBinding() {
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		MyEntity bean = new MyEntity();
		binder.setBean(bean);
		age.setValue("1");
		BindingValidationStatus<String> s = binding.validate();
		assertTrue(s.getResult().isPresent());
		assertFalse(s.getResult().get().isError());
	}

	@Test
	public void testValidateBindingValidationError() {
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		MyEntity bean = new MyEntity();
		binder.setBean(bean);
		age.setValue("-11");
		BindingValidationStatus<String> s = binding.validate();
		assertTrue(s.getResult().isPresent());
		assertTrue(s.getResult().get().isError());
	}

	@Test
	public void testValidateBindingConversionError() {
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		MyEntity bean = new MyEntity();
		binder.setBean(bean);
		age.setValue("nan");
		BindingValidationStatus<String> s = binding.validate();
		assertTrue(s.getResult().isPresent());
		assertTrue(s.getResult().get().isError());
	}

	@Test
	public void testBindingFindLocaleComponentWithLocale() {
		age.setLocale(Locale.CANADA);
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		assertEquals(Locale.CANADA, binding.findLocale());
	}

	@Test
	public void testBindingFindLocaleFromUI() {
		UI mockUi = mock(UI.class);
		when(mockUi.getLocale()).thenReturn(Locale.FRANCE);
		UI.setCurrent(mockUi);
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		assertEquals(Locale.FRANCE, binding.findLocale());
	}

	@Test
	public void testBindingFindLocaleComponentNoGlobal() {
		UI mockUi = mock(UI.class);
		when(mockUi.getLocale()).thenReturn(null);
		UI.setCurrent(mockUi);
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		assertEquals(Locale.getDefault(), binding.findLocale());
	}

	@Test
	public void testBindingFindLocaleDefault() {
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		assertEquals(Locale.getDefault(), binding.findLocale());
	}


	@Test
	public void testGetGetter() {
		EasyBinding<MyEntity, String, Integer> binding = binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		assertNotNull(binding.getGetter());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAssignUnassign() {
		BinderStatusChangeListener statusChangeListener = mock(BinderStatusChangeListener.class);
		ValueChangeListener<?> valueChangeListener = mock(ValueChangeListener.class);
		binder.addStatusChangeListener(statusChangeListener);
		binder.addValueChangeListener(valueChangeListener);

		verify(statusChangeListener, never()).statusChange(any());

		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));

		//verify(statusChangeListener, never()).statusChange(any());
		//verify(statusChangeListener, times(1)).statusChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(any());
		verify(valueChangeListener, never()).valueChange(any());

		reset(statusChangeListener);

		MyEntity e = new MyEntity();

		binder.setBean(e);

		assertFalse(binder.isValid());
		assertFalse(binder.getHasChanges());

		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasValidationErrors())));
		verify(valueChangeListener, never()).valueChange(any());

		reset(statusChangeListener);

		lastName.setValue("giraf");

		verify(valueChangeListener, times(1)).valueChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasValidationErrors())));
		assertTrue(binder.getHasChanges());

		reset(valueChangeListener);
		reset(statusChangeListener);

		firstName.setValue("giraf");

		verify(valueChangeListener, times(1)).valueChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasConversionErrors())));

		assertTrue(binder.getHasChanges());

		reset(valueChangeListener);
		reset(statusChangeListener);

		age.setValue("nan");

		//verify(valueChangeListener, times(1)).valueChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasConversionErrors())));

		reset(statusChangeListener);

		binder.removeBean();

		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasConversionErrors())));

		reset(statusChangeListener);

		//form.age.setValue("100");

		//verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasConversionErrors())));

	}

	@Test
	public void testReadonly() {
		assertFalse(firstName.isReadOnly());
		assertFalse(lastName.isReadOnly());
		assertFalse(age.isReadOnly());

		binder.bind(firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		binder.bind(age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));

		binder.setReadonly(true);

		assertTrue(firstName.isReadOnly());
		assertTrue(lastName.isReadOnly());
		assertTrue(age.isReadOnly());

		binder.setReadonly(false);

		assertFalse(firstName.isReadOnly());
		assertFalse(lastName.isReadOnly());
		assertFalse(age.isReadOnly());
	}

	@Test
	public void testReadOnlyBinding() {
		MyEntity entity = new MyEntity();

		binder.bind(firstName, e -> e.getFirstName(), null, "firstName", new NullConverter<>(""));
		binder.bind(lastName, MyEntity::getLastName, null, "lastName", new NullConverter<>(""));
		binder.bind(age, MyEntity::getAge, null, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));

		entity.setFirstName("John");
		entity.setLastName("Doe");
		entity.setAge(50);

		binder.setBean(entity);

		firstName.setValue("Carl");
		assertEquals("John", entity.getFirstName());

		assertTrue(firstName.isReadOnly());
		assertTrue(lastName.isReadOnly());
		assertTrue(age.isReadOnly());

		binder.setReadonly(false);

		assertTrue(firstName.isReadOnly());
		assertTrue(lastName.isReadOnly());
		assertTrue(age.isReadOnly());
	}

	@Test
	public void testGetConstraintViolations() {
		assertEquals(0, binder.getConstraintViolations().size());
		binder.bind(firstName, e -> e.getFirstName(), null, "firstName", new NullConverter<>(""));
		assertEquals(0, binder.getConstraintViolations().size());
		binder.setBean(new MyEntity());
		assertEquals(1, binder.getConstraintViolations().size());
	}

	@Test
	public void testGroupValidationExplicitGroup() {
		@SuppressWarnings("unchecked")
		HasValue<String> field1 = mock(HasValue.class);
		Label statusLabel = mock(Label.class);

		BasicBinder<MyEntity2> binder = new BasicBinder<>();

		binder.bind(field1, d -> d.getS1() == null ? "" : d.getS1(), (e, f) -> e.setS1("".equals(f) ? null : f), "s1");

		binder.setStatusLabel(statusLabel);

		MyEntity2 bean = new MyEntity2();

		binder.setBean(bean);

		assertTrue(binder.isValid());

		binder.setValidationGroups(MyGroup.class);

		assertFalse(binder.isValid());

		assertEquals(1, binder.getValidationGroups().length);

		binder.clearValidationGroups();

		assertTrue(binder.isValid());
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = MyValidator.class)
	public static @interface MyEntityValid {
		String message() default "At least one field must be set";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class MyValidator implements ConstraintValidator<MyEntityValid, MyEntityBeanLevel> {

		@Override
		public void initialize(MyEntityValid constraintAnnotation) {
		}

		@Override
		public boolean isValid(MyEntityBeanLevel value, ConstraintValidatorContext context) {
			return value.s1 != null || value.s2 != null;
		}
	}

	@MyEntityValid
	public static class MyEntityBeanLevel {
		String s1;
		String s2;

		public String getS1() {
			return s1;
		}

		public void setS1(String s1) {
			this.s1 = s1;
		}

		public String getS2() {
			return s2;
		}

		public void setS2(String s2) {
			this.s2 = s2;
		}

	}

	@Test
	public void testBeanClassLevelValidation() {
		TextField field1 = new TextField();
		TextField field2 = new TextField();
		Label statusLabel = new Label();

		BasicBinder<MyEntityBeanLevel> binder = new BasicBinder<>();

		BinderStatusChangeListener statusChangeListener = mock(BinderStatusChangeListener.class);
		binder.addStatusChangeListener(statusChangeListener);

		binder.bind(field1, d -> d.getS1() == null ? "" : d.getS1(), (e, f) -> e.setS1("".equals(f) ? null : f), "s1");
		binder.bind(field2, d -> d.getS2() == null ? "" : d.getS2(), (e, f) -> e.setS2("".equals(f) ? null : f), "s2");

		binder.setStatusLabel(statusLabel);

		reset(statusChangeListener);

		MyEntityBeanLevel bean = new MyEntityBeanLevel();

		binder.setBean(bean);

		assertFalse(binder.isValid());
		assertEquals("At least one field must be set", statusLabel.getValue());

		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasValidationErrors())));

		reset(statusChangeListener);

		field2.setValue("Test");

		verify(statusChangeListener, times(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));

		assertTrue(binder.isValid());
		assertEquals("", statusLabel.getValue());

	}

	@Test
	public void testBeanClassLevelValidationNoStatusLabel() {
		TextField field1 = new TextField();
		TextField field2 = new TextField();

		BasicBinder<MyEntityBeanLevel> binder = new BasicBinder<>();

		binder.bind(field1, d -> d.getS1() == null ? "" : d.getS1(), (e, f) -> e.setS1("".equals(f) ? null : f), "s1");
		binder.bind(field2, d -> d.getS2() == null ? "" : d.getS2(), (e, f) -> e.setS2("".equals(f) ? null : f), "s2");

		MyEntityBeanLevel bean = new MyEntityBeanLevel();

		binder.setBean(bean);

		assertFalse(binder.isValid());
	}

}
