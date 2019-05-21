package org.vaadin.easybinder.usagetest.com.vaadin.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.Test;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.ui.TextField;

public class BeanValidationBinderNumberTest {

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

		Binder<MyEntity> binder = new BeanValidationBinder<>(MyEntity.class);
		binder.forField(number).withNullRepresentation("")
				.withConverter(new StringToIntegerConverter("Must be a number")).bind("number");

		MyEntity t = new MyEntity();

		// valid
		t.setNumber(1);
		binder.setBean(t);

		assertTrue(validator.validate(t).isEmpty());
		assertTrue(binder.validate().isOk());

		// invalid
		t.setNumber(0);
		binder.setBean(t);
		assertFalse(validator.validate(t).isEmpty());
		assertFalse(binder.validate().isOk());

		t.setNumber(null);
		binder.setBean(t);
		assertFalse(validator.validate(t).isEmpty());
		assertFalse(binder.validate().isOk());

		assertEquals("", number.getValue());
		number.setValue("1");
		assertEquals(Integer.valueOf(1), t.getNumber());
		number.setValue("");

		// Since validation fails the value is still "1" and not null (also kind of
		// hinted at in https://github.com/vaadin/framework/issues/9453)
		// See Binder.BinderImpl.writeFieldValue()

		// assertEquals(null, t.getNumber());
		assertEquals(Integer.valueOf(1), t.getNumber());
	}
}
