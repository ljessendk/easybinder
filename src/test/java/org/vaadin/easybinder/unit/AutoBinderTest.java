package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.validation.constraints.Min;

import org.junit.Test;
import org.vaadin.easybinder.AutoBinder;

import com.vaadin.data.HasValue;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.TextField;

public class AutoBinderTest {
	public static class MyForm {
		TextField street = new TextField();
		TextField number = new TextField();
		TextField number2 = new TextField();
	}

	public static class MyEntity {
		String street;

		@Min(value = 1)
		Integer number;

		int number2;

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
		}

		public int getNumber2() {
			return number2;
		}

		public void setNumber2(int number) {
			this.number2 = number;
		}

	}

	@Test
	public void testValid() {
		MyForm form = new MyForm();
		AutoBinder<MyEntity> binder = new AutoBinder<>(MyEntity.class);

		binder.bindInstanceFields(form);

		MyEntity entity = new MyEntity();
		binder.setBean(entity);

		form.street.setValue("mystreet");
		form.number.setValue("100");

		assertEquals(new Integer(100), entity.getNumber());
		assertEquals("mystreet", entity.getStreet());

		assertNull(form.number.getComponentError());
		form.number.setValue("0");
		assertNotNull(form.number.getComponentError());

		form.number2.setValue("");
		assertNotNull(form.number2.getComponentError());
		form.number2.setValue("2");
		assertNull(form.number2.getComponentError());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBuildAndBind() {
		AutoBinder<MyEntity> binder = new AutoBinder<>(MyEntity.class);
		binder.buildAndBind();

		MyEntity entity = new MyEntity();
		binder.setBean(entity);

		assertTrue(binder.getFieldForProperty("street").isPresent());
		assertTrue(binder.getFieldForProperty("number").isPresent());
		assertTrue(binder.getFieldForProperty("number2").isPresent());

		AbstractField<String> numberField = (AbstractField<String>) binder.getFieldForProperty("number").get();
		AbstractField<String> numberField2 = (AbstractField<String>) binder.getFieldForProperty("number2").get();

		((HasValue<String>) binder.getFieldForProperty("street").get()).setValue("mystreet");
		assertEquals("mystreet", entity.getStreet());

		numberField.setValue("100");
		assertEquals(new Integer(100), entity.getNumber());

		assertNull(numberField.getComponentError());
		numberField.setValue("0");
		assertNotNull(numberField.getComponentError());
		numberField.setValue("");

		assertEquals(null, entity.getNumber());
		assertNull(numberField.getComponentError());

		numberField2.setValue("");
		assertNotNull(numberField2.getComponentError());
	}

}
