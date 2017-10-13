package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;
import org.vaadin.easybinder.ui.EComboBox;

import com.vaadin.data.Converter;
import com.vaadin.ui.RadioButtonGroup;

/*
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;

import org.junit.Test;
import org.vaadin.easybinder.ReflectionBinder;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.data.HasValue;
import com.vaadin.ui.RadioButtonGroup;
*/

public class ReflectionBinderTest {
	static enum TestEnum {
		Test1,
		Test2
	}
		
	static class TestEntity{
		static class UnknownEntity {
			int a;
		}
		
	}
	
	ReflectionBinder<TestEntity> binder = new ReflectionBinder<>(TestEntity.class);
	
	
	@Test
	public void testStringConverterNullValue() {
		Converter<String,?> converter = binder.createStringConverter();
		assertEquals("", converter.convertToPresentation(null, null));
	}
	
	@Test
	public void testStringConverterNonNullValue() {
		Converter<String,Object> converter = binder.createStringConverter();
		assertEquals("test", converter.convertToPresentation(new Object() {
			@Override
			public String toString() {
				return "test";
			}
		}, null));
		
	}	
	
	@Test
	public void testGetFieldTypeForAnonymousInstanceOfGenericField() {
		@SuppressWarnings("serial")
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>() {};
		assertTrue(binder.getFieldTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getFieldTypeForField(r).get());		
	}
	
	@Test
	public void testGetFieldTypeForHasGenericType() {
		EComboBox<TestEnum> r = new EComboBox<>(TestEnum.class);
		assertTrue(binder.getFieldTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getFieldTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForGenericFieldWithValue() {
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>();
		r.setValue(TestEnum.Test1);
		assertTrue(binder.getFieldTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getFieldTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForGenericFieldWithItems() {
		EnumSet<TestEnum> set = EnumSet.allOf(TestEnum.class); 
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>("", set);
		assertTrue(binder.getFieldTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getFieldTypeForField(r).get());		
	}

	
	
	/*
	@Test
	public void test() {
		RadioButtonGroup<FlightId.LegType> r = new RadioButtonGroup<>();
		HasValue<FlightId.LegType> p = r;
		Class<HasValue<?>> c = (Class<HasValue<?>>)p.getClass(); 
		System.out.println("class is: " + c);
		Type valueType = GenericTypeReflector.getTypeParameter(c, HasValue.class.getTypeParameters()[0]);

		assertNotNull(valueType);
	}
	*/
	
}
