package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.vaadin.easybinder.BasicBinder;
import org.vaadin.easybinder.NullConverter;

import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.ui.TextField;

public class BasicBinderNumberTest {
	public static class MyEntity {
		@Min(value = 1)
		@NotNull
		Integer number;

		public void setNumber(Integer number) {
			this.number = number;
		}

		public Integer getNumber() {
			return number;
		}
	}

	@Test
	public void testValid() {
		TextField number = new TextField();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(number, d -> d.getNumber() == null ? "" : Integer.toString(d.getNumber()),
				(e, f) -> e.setNumber("".equals(f) ? null : Integer.parseInt(f)), "number");
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
	public void testValid2() {
		TextField number = new TextField();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(number, d -> d.getNumber(), (e, f) -> e.setNumber(f), "number",
				new NullConverter<String>("").chain(new StringToIntegerConverter("Conversion failed")));

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

}
