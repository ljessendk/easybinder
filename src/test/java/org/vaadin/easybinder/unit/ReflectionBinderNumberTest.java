package org.vaadin.easybinder.unit;

import static org.junit.Assert.*;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.vaadin.easybinder.ReflectionBinder;

import com.vaadin.ui.TextField;

public class ReflectionBinderNumberTest {
	public static class MyEntity {
		@Min(value = 1)
		@NotNull
		Integer number;

		int primitiveNumber;

		public void setNumber(Integer number) {
			this.number = number;
		}

		public Integer getNumber() {
			return number;
		}

		public void setPrimitiveNumber(int number) {
			this.primitiveNumber = number;
		}

		public int getPrimitiveNumber() {
			return primitiveNumber;
		}
	}

	@Test
	public void testValid() {
		TextField number = new TextField();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		ReflectionBinder<MyEntity> binder = new ReflectionBinder<>(MyEntity.class);

		binder.bind(number, "number");
		/*
		 * binder.forField(number) .withNullRepresentation("") .withConverter(new
		 * StringToIntegerConverter("Must be a number")) .bind("number");
		 */
		MyEntity t = new MyEntity();

		// valid
		t.setNumber(1);
		binder.setBean(t);

		assertTrue(validator.validate(t).isEmpty());
		assertTrue(binder.isValid());

		// invalid
		t.setNumber(0);
		binder.setBean(t);
		assertFalse(validator.validate(t).isEmpty());
		assertFalse(binder.isValid());

		t.setNumber(null);
		binder.setBean(t);
		assertFalse(validator.validate(t).isEmpty());
		assertFalse(binder.isValid());

		assertEquals("", number.getValue());
		number.setValue("1");
		assertEquals(new Integer(1), t.getNumber());
		number.setValue("");

		assertEquals(null, t.getNumber());
	}

	@Test
	public void testPrimitiveNumberBinding() {
		TextField primitiveNumber = new TextField();

		ReflectionBinder<MyEntity> binder = new ReflectionBinder<>(MyEntity.class);

		binder.bind(primitiveNumber, "primitiveNumber");

		binder.setBean(new MyEntity());

		primitiveNumber.setValue("");
		assertNotNull(primitiveNumber.getComponentError());
		primitiveNumber.setValue("1");
		assertNull(primitiveNumber.getComponentError());
		primitiveNumber.setValue("abc");
		assertNotNull(primitiveNumber.getComponentError());
	}

}
