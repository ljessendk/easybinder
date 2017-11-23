package org.vaadin.easybinder.usagetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

	BasicBinder<MyEntity> binder = new BasicBinder<>();
	BasicBinder<MyEntitySizeMin1> binderSizeMin = new BasicBinder<>();

	@Test
	public void testStringNullRepresentation() {
		TextField field = new TextField();

		// Bind with null representation
		binder.bind(field, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		MyEntity bean = new MyEntity();

		assertTrue(binder.isValid());

		binder.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(binder.isValid());
	}

	@Test
	public void testNullRepresentationSizeMin1() {
		TextField field = new TextField();

		// Bind with null representation
		binderSizeMin.bind(field, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		assertTrue(binder.isValid());

		binderSizeMin.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		field.setValue("");
		assertNull(bean.getText());

		assertTrue(binderSizeMin.isValid());
	}

	@Test
	public void testSizeMin1() {
		TextField field = new TextField();

		binderSizeMin.bind(field, d -> d.getText(), (e, f) -> e.setText(f), "text");
		field.setValue("reset");

		MyEntitySizeMin1 bean = new MyEntitySizeMin1();

		bean.setText("");
		binderSizeMin.setBean(bean);

		field.setValue("test");
		assertEquals("test", bean.getText());

		assertTrue(binder.isValid());

		field.setValue("");
		assertFalse(binderSizeMin.isValid());
		assertEquals("", bean.getText());

	}

	@Test
	public void testValid() {
		TextField text = new TextField();

		binder.bind(text, d -> d.getText() == null ? "" : d.getText(), (e, f) -> e.setText("".equals(f) ? null : f),
				"text");

		MyEntity t = new MyEntity();

		t.setText("bla");
		binder.setBean(t);

		assertTrue(binder.isValid());

		text.setValue("");
		assertNull(t.getText());
	}

}
