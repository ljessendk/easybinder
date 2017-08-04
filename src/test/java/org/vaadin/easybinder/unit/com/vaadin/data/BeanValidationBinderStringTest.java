package org.vaadin.easybinder.unit.com.vaadin.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;

import org.junit.Test;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.ui.TextField;

public class BeanValidationBinderStringTest {

	public static class MyEntity {
		String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public static class MyEntitySizeMin1 {
		@Size(min = 1)
		String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	Validator validator = factory.getValidator();

	@Test
	public void testNullRepresentation() {
		TextField field = new TextField();

		// Bind with null representation
		Binder<MyEntity> binder = new BeanValidationBinder<>(MyEntity.class);
		binder.forField(field).withNullRepresentation("").bind("text");

		MyEntity bean = new MyEntity();

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.validate().isOk());

		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.validate().isOk());
	}

	@Test
	public void testNullRepresentationSizeMin1() {
		TextField field = new TextField();

		// Bind with null representation
		Binder<MyEntitySizeMin1> binder = new BeanValidationBinder<>(MyEntitySizeMin1.class);
		binder.forField(field).withNullRepresentation("").bind("text");

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.validate().isOk());

		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.validate().isOk());
	}

	@Test
	public void testSizeMin1() {
		TextField field = new TextField();

		// Bind with null representation
		Binder<MyEntitySizeMin1> binder = new BeanValidationBinder<>(MyEntitySizeMin1.class);
		binder.forField(field).bind("text");

		field.setValue("reset");

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		// Why doesn't this fail when there is no null representation?
		// https://github.com/vaadin/framework/issues/9453
		binder.setBean(bean);

		System.out.println("Field value: " + field.getValue());

		assertTrue(validator.validate(bean).isEmpty());

		field.setValue("test");
		assertEquals("test", bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.validate().isOk());

		field.setValue("");
		assertFalse(binder.validate().isOk());

		// Since validation fails the value is still "test" and not "" (also kind of
		// hinted at in https://github.com/vaadin/framework/issues/9453)
		// See Binder.BinderImpl.writeFieldValue()

		// assertEquals("", bean.getText());
	}

	@Test
	public void testValid() {
		TextField text = new TextField();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Binder<MyEntity> binder = new BeanValidationBinder<>(MyEntity.class);
		binder.forField(text).withNullRepresentation("").bind("text");

		MyEntity t = new MyEntity();

		// valid
		t.setText("bla");
		binder.setBean(t);

		assertTrue(validator.validate(t).isEmpty());
		assertTrue(binder.validate().isOk());

		text.setValue("");
		assertNull(t.getText());

		/*
		 * // invalid t.setText(""); binder.setBean(t);
		 * assertFalse(validator.validate(t).isEmpty());
		 * assertFalse(binder.validate().isOk());
		 * 
		 * t.setText(null); binder.setBean(t);
		 * assertTrue(validator.validate(t).isEmpty());
		 * assertTrue(binder.validate().isOk());
		 */
	}
}
