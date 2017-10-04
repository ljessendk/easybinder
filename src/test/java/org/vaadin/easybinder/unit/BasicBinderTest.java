package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.vaadin.easybinder.BasicBinder;
import org.vaadin.easybinder.BasicBinder.EasyBinding;
import org.vaadin.easybinder.BinderStatusChangeListener;
import org.vaadin.easybinder.NullConverter;
import org.vaadin.easybinder.StringLengthConverterValidator;

import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.ui.TextField;
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;

public class BasicBinderTest {
	
	public class MyEntity {
		@NotNull
		String firstName;
		String lastName;
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
	
	public class MyForm {
		TextField firstName = new TextField();
		TextField lastName = new TextField();
		TextField age = new TextField();
	}
	
	@Test
	public void testBindUnbind() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		
		assertEquals(2, binder.getFields().collect(Collectors.toList()).size());

		assertEquals(form.firstName, binder.getFieldForProperty("firstName").get());
		assertEquals(form.lastName, binder.getFieldForProperty("lastName").get());
		
		binder.removeBinding(form.firstName);
		
		assertEquals(1, binder.getFields().collect(Collectors.toList()).size());

		assertFalse(binder.getFieldForProperty("firstName").isPresent());
		assertEquals(form.lastName, binder.getFieldForProperty("lastName").get());
		
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");

		assertEquals(2, binder.getFields().collect(Collectors.toList()).size());
		
		binder.unbind();
		
		assertFalse(binder.getFieldForProperty("firstName").isPresent());
		assertFalse(binder.getFieldForProperty("lastName").isPresent());		
		
		EasyBinding<MyEntity, ?, ?> b1 = binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");

		assertEquals(2, binder.getFields().collect(Collectors.toList()).size());

		binder.getBinding("lastName").ifPresent(e -> binder.removeBinding(e));
		
		assertEquals(1, binder.getFields().collect(Collectors.toList()).size());
				
		binder.removeBinding(b1);
		
		assertEquals(0, binder.getFields().collect(Collectors.toList()).size());		
		
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");

		assertEquals(1, binder.getFields().collect(Collectors.toList()).size());
		
		binder.removeBinding("lastName");

		assertEquals(0, binder.getFields().collect(Collectors.toList()).size());
	}

	@Test
	public void testSetBeanBeforeBind() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		MyEntity entity = new MyEntity();
		entity.setLastName("Doe");
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));		
		binder.setBean(entity);

		assertFalse(binder.isValid());	
		
		assertEquals("Doe", form.lastName.getValue());		
	}	
	
	@Test
	public void testNoPropertyProvided() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		MyEntity entity = new MyEntity();
		binder.setBean(entity);
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), null, new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, null, new NullConverter<>(""));

		assertFalse(binder.isValid());			
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAssignUnassign() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		BinderStatusChangeListener statusChangeListener = mock(BinderStatusChangeListener.class);
		ValueChangeListener<?> valueChangeListener = mock(ValueChangeListener.class);
		binder.addStatusChangeListener(statusChangeListener);
		binder.addValueChangeListener(valueChangeListener);
		
		verify(statusChangeListener, never()).statusChange(any());
		
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		binder.bind(form.age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));

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
		
		form.lastName.setValue("giraf");

		verify(valueChangeListener, times(1)).valueChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasValidationErrors())));		
		assertTrue(binder.getHasChanges());
		
		reset(valueChangeListener);
		reset(statusChangeListener);
		
		form.firstName.setValue("giraf");

		verify(valueChangeListener, times(1)).valueChange(any());
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertFalse(sc.hasConversionErrors())));
		
		assertTrue(binder.getHasChanges());

		reset(valueChangeListener);
		reset(statusChangeListener);
		
		form.age.setValue("nan");
		
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
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		
		assertFalse(form.firstName.isReadOnly());
		assertFalse(form.lastName.isReadOnly());
		assertFalse(form.age.isReadOnly());
		
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		binder.bind(form.age, MyEntity::getAge, MyEntity::setAge, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));

		binder.setReadonly(true);
		
		assertTrue(form.firstName.isReadOnly());
		assertTrue(form.lastName.isReadOnly());
		assertTrue(form.age.isReadOnly());
		
		binder.setReadonly(false);
		
		assertFalse(form.firstName.isReadOnly());
		assertFalse(form.lastName.isReadOnly());
		assertFalse(form.age.isReadOnly());		
	}	
	
	@Test
	public void testReadOnlyBinding() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();

		MyEntity entity = new MyEntity();
		
		assertFalse(form.firstName.isReadOnly());
		assertFalse(form.lastName.isReadOnly());
		assertFalse(form.age.isReadOnly());
		
		binder.bind(form.firstName, e -> e.getFirstName(), null, "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, null, "lastName", new NullConverter<>(""));
		binder.bind(form.age, MyEntity::getAge, null, "age", new StringLengthConverterValidator("Must be a number", 1, null).chain(new StringToIntegerConverter("Must be a number")));
		
		entity.setFirstName("John");
		entity.setLastName("Doe");
		entity.setAge(50);

		binder.setBean(entity);

		form.firstName.setValue("Carl");
		assertEquals("John", entity.getFirstName());
		
		assertTrue(form.firstName.isReadOnly());
		assertTrue(form.lastName.isReadOnly());
		assertTrue(form.age.isReadOnly());
		
		binder.setReadonly(false);
		
		assertTrue(form.firstName.isReadOnly());
		assertTrue(form.lastName.isReadOnly());
		assertTrue(form.age.isReadOnly());
	}	
}
