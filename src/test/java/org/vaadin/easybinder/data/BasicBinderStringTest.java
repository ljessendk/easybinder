package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;

import org.junit.Test;
import org.vaadin.easybinder.data.BasicBinder;

import com.vaadin.ui.TextField;

public class BasicBinderStringTest {
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
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(field, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		MyEntity bean = new MyEntity();

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());

		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());
	}

	@Test
	public void testNullRepresentationSizeMin1() {
		TextField field = new TextField();

		// Bind with null representation
		BasicBinder<MyEntitySizeMin1> binder = new BasicBinder<>();
		binder.bind(field, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		/*
		 * binder.forField(field) .withNullRepresentation("") .bind("text");
		 */

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());

		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());
	}

	@Test
	public void testSizeMin1() {
		TextField field = new TextField();

		BasicBinder<MyEntitySizeMin1> binder = new BasicBinder<>();
		binder.bind(field, d -> d.getText(), (e, f) -> e.setText(f), "text");
		/*
		 * binder.forField(field) .bind("text");
		 */
		field.setValue("reset");

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		bean.setText("");
		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());

		field.setValue("");
		assertFalse(binder.isValid());
		assertEquals("", bean.getText());

	}

	@Test
	public void testValid() {
		TextField text = new TextField();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(text, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		/*
		 * binder.forField(text) .withNullRepresentation("") .bind("text");
		 */
		MyEntity t = new MyEntity();

		// valid
		t.setText("bla");
		binder.setBean(t);

		assertTrue(validator.validate(t).isEmpty());
		assertTrue(binder.isValid());

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
