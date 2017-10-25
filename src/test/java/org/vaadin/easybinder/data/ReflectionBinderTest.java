package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;
import org.vaadin.easybinder.data.BasicBinder.EasyBinding;
import org.vaadin.easybinder.ui.EComboBox;
import org.vaadin.easybinder.ui.EGTypeComponentAdapter;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;

public class ReflectionBinderTest {
	static enum TestEnum {
		Test1,
		Test2
	}
		
	static class TestEntity{
		String testString;
		int testInt;
		Integer testInteger;
		Set<String> testSet;
				
		public String getTestString() {
			return testString;
		}
		public void setTestString(String testString) {
			this.testString = testString;
		}
		public int getTestInt() {
			return testInt;
		}
		public void setTestInt(int testInt) {
			this.testInt = testInt;
		}
		public Integer getTestInteger() {
			return testInteger;
		}
		public void setTestInteger(Integer testInteger) {
			this.testInteger = testInteger;
		}
		public Set<String> getTestSet() {
			return testSet;
		}
		public void setTestSet(Set<String> testSet) {
			this.testSet = testSet;
		}
	}
	
	TextField testString = new TextField();
	TextField testInt = new TextField();
	TextField testInteger = new TextField();

	ConverterRegistry converterRegistry = mock(ConverterRegistry.class);
	ReflectionBinder<TestEntity> binder = new ReflectionBinder<>(TestEntity.class, converterRegistry);
	
	@Test
	public void testStringConverterNullValue() {
		Converter<String,?> converter = binder.createToStringConverter();
		assertEquals("", converter.convertToPresentation(null, null));
	}
	
	@Test
	public void testStringConverterNonNullValue() {
		Converter<String,Object> converter = binder.createToStringConverter();
		assertEquals("test", converter.convertToPresentation(new Object() {
			@Override
			public String toString() {
				return "test";
			}
		}, null));		
	}
	
	@Test
	public void testCastingConverterPrimitiveType() {
		Converter<Integer, Integer> intConverter = binder.createCastConverter(int.class);
		Result<Integer> res = intConverter.convertToModel(10, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), intConverter.convertToPresentation(new Integer(21), null));
	}

	@Test
	public void testCastingConverterNonPrimitiveType() {
		Converter<Integer, Integer> intConverter = binder.createCastConverter(Integer.class);
		Result<Integer> res = intConverter.convertToModel(new Integer(10), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), intConverter.convertToPresentation(new Integer(21), null));
	}

	@Test
	public void testGetFieldTypeForAnonymousInstanceOfGenericField() {
		@SuppressWarnings("serial")
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>() {};
		assertTrue(binder.getPresentationTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getPresentationTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForHasGenericType() {
		EComboBox<TestEnum> r = new EComboBox<>(TestEnum.class);
		assertTrue(binder.getPresentationTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getPresentationTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForGenericFieldWithValue() {
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>();
		r.setValue(TestEnum.Test1);
		assertTrue(binder.getPresentationTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getPresentationTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForGenericFieldWithItems() {
		EnumSet<TestEnum> set = EnumSet.allOf(TestEnum.class); 
		RadioButtonGroup<TestEnum> r = new RadioButtonGroup<TestEnum>("", set);
		assertTrue(binder.getPresentationTypeForField(r).isPresent());		
		assertEquals(TestEnum.class, binder.getPresentationTypeForField(r).get());		
	}

	@Test
	public void testGetFieldTypeForAnonymousInstanceOfGenericCollectionField() {
		@SuppressWarnings("serial")
		TwinColSelect<String> r = new TwinColSelect<String>() {};
		assertTrue(binder.getPresentationTypeForField(r).isPresent());
		assertEquals(Set.class, binder.getPresentationTypeForField(r).get());
	}

	@Test
	public void testGetFieldTypeForHasGenericTypeOfGenericCollectionField() {
		@SuppressWarnings({ "rawtypes" })
		EGTypeComponentAdapter<Set> r = new EGTypeComponentAdapter<>(Set.class, new TwinColSelect<String>());
		assertTrue(binder.getPresentationTypeForField(r).isPresent());
		assertEquals(Set.class, binder.getPresentationTypeForField(r).get());
	}

	@Test
	public void testBindNoSuchField() {
		try {
			binder.bind(new TextField(), "noSuchField");
			assertTrue(false);
		} catch(IllegalArgumentException ex) {
		}
	}

	@Test
	public void testBindNoConverterIdentity() {
		when(converterRegistry.getConverter(String.class, String.class)).thenReturn(null);
		EasyBinding<TestEntity, String, String> binding = binder.bind(new TextField(), "testString");
		assertNotNull(binding);
	}

	@Test
	public void testBindNoConverterUnrelatedStringPresentation() {
		when(converterRegistry.getConverter(String.class, Integer.class)).thenReturn(null);
		EasyBinding<TestEntity, String, Integer> binding = binder.bind(new TextField(), "testInt");
		assertNotNull(binding);
	}

	@Test(expected = RuntimeException.class)
	public void testBindNoConverterUnrelatedNonStringPresentation() {
		when(converterRegistry.getConverter(Double.class, int.class)).thenReturn(null);
		binder.bind(new Slider(), "testInt");
	}

	@Test
	public void testBindTypeErasure() {
		when(converterRegistry.getConverter(String.class, String.class)).thenReturn(null);
		EasyBinding<TestEntity, String, String> binding = binder.bind(new RadioButtonGroup<String>(), "testString");
		assertNotNull(binding);
		verify(converterRegistry, never()).getConverter(any(), any());

		Result<String> res = binding.converterValidatorChain.convertToModel("giraf", null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals("giraf", e));
		assertEquals("bird", binding.converterValidatorChain.convertToPresentation("bird", null));
	}

	@Test
	public void testBindSet() {
		when(converterRegistry.getConverter(Set.class, Set.class)).thenReturn(null);
		@SuppressWarnings("serial")
		EasyBinding<TestEntity, Set<String>, Set<String>> binding = binder.bind(new TwinColSelect<String>(){}, "testSet");
		assertNotNull(binding);
		verify(converterRegistry, times(1)).getConverter(Set.class, Set.class);
	}

	@Test
	public void testCreateConverterPrimitiveToPrimitive() {
		int emptyValue = 0;

		when(converterRegistry.getConverter(int.class, int.class)).thenReturn(null);		
		Converter<Integer, Integer> converter = binder.createConverter(int.class, int.class, emptyValue);
		assertNotNull(converter);
		Result<Integer> res = converter.convertToModel(10, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), converter.convertToPresentation(21, null));

		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(emptyValue), e));
		assertEquals(new Integer(emptyValue), converter.convertToPresentation(emptyValue, null));		
	}

	@Test
	public void testCreateConverterPrimitiveToNonPrimitive() {
		int emptyValue = 0;

		when(converterRegistry.getConverter(int.class, Integer.class)).thenReturn(null);
		Converter<Integer, Integer> converter = binder.createConverter(int.class, Integer.class, emptyValue);
		assertNotNull(converter);
		Result<Integer> res = converter.convertToModel(new Integer(10), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), converter.convertToPresentation(new Integer(21), null));

		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));
		assertEquals(new Integer(emptyValue), converter.convertToPresentation(null, null));		
	}

	@Test
	public void testCreateConverterNonPrimitiveToPrimitive() {
		Integer emptyValue = null;

		when(converterRegistry.getConverter(Integer.class, int.class)).thenReturn(null);		
		Converter<Integer, Integer> converter = binder.createConverter(Integer.class, int.class, emptyValue);
		assertNotNull(converter);
		Result<Integer> res = converter.convertToModel(new Integer(10), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), converter.convertToPresentation(new Integer(21), null));

		res = converter.convertToModel(emptyValue, null);
		// Should fail
		assertTrue(res.isError());
	}	

	@Test
	public void testCreateConverterNonPrimitiveToNonPrimitive() {
		Integer emptyValue = null;

		when(converterRegistry.getConverter(Integer.class, Integer.class)).thenReturn(null);		
		Converter<Integer, Integer> converter = binder.createConverter(Integer.class, Integer.class, emptyValue);
		assertNotNull(converter);
		Result<Integer> res = converter.convertToModel(new Integer(10), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), converter.convertToPresentation(new Integer(21), null));

		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));

		assertEquals(emptyValue, converter.convertToPresentation(null, null));		
	}	

	@Test
	public void testCreateConverterNonPrimitiveToNonPrimitiveEmptyValue() {
		Integer emptyValue = new Integer(0);

		when(converterRegistry.getConverter(Integer.class, Integer.class)).thenReturn(null);		
		Converter<Integer, Integer> converter = binder.createConverter(Integer.class, Integer.class, emptyValue);
		assertNotNull(converter);
		Result<Integer> res = converter.convertToModel(new Integer(10), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(new Integer(10), e));
		assertEquals(new Integer(21), converter.convertToPresentation(new Integer(21), null));

		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));

		assertEquals(emptyValue, converter.convertToPresentation(null, null));		
	}	
	
	@Test
	public void testCreateConverterStringToString() {
		String emptyValue = null;

		when(converterRegistry.getConverter(String.class, String.class)).thenReturn(null);		
		Converter<String, String> converter = binder.createConverter(String.class, String.class, emptyValue);
		assertNotNull(converter);
		Result<String> res = converter.convertToModel("abc", null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals("abc", e));
		assertEquals("def", converter.convertToPresentation("def", null));
		
		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));

		assertEquals(emptyValue, converter.convertToPresentation(null, null));		
	}	

	@Test
	public void testCreateConverterStringToStringEmpty() {
		String emptyValue = "";
		
		when(converterRegistry.getConverter(String.class, String.class)).thenReturn(null);		
		Converter<String, String> converter = binder.createConverter(String.class, String.class, emptyValue);
		assertNotNull(converter);
		Result<String> res = converter.convertToModel("abc", null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals("abc", e));
		assertEquals("def", converter.convertToPresentation("def", null));
		
		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));

		assertEquals(emptyValue, converter.convertToPresentation(null, null));		
	}
	
	@Test
	public void testCreateConverterBooleanToboolean() {
		Boolean emptyValue = false;
		
		when(converterRegistry.getConverter(Boolean.class, boolean.class)).thenReturn(null);		
		Converter<Boolean, Boolean> converter = binder.createConverter(Boolean.class, boolean.class, emptyValue);
		assertNotNull(converter);
		Result<Boolean> res = converter.convertToModel(new Boolean(false), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(false, e));
		res = converter.convertToModel(new Boolean(true), null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(true, e));
		
		assertEquals(false, converter.convertToPresentation(false, null));
		assertEquals(true, converter.convertToPresentation(true, null));
	}
	
	@Test
	public void testCreateConverterEnumToEnum() {
		TestEnum emptyValue = null;
		
		when(converterRegistry.getConverter(TestEnum.class, TestEnum.class)).thenReturn(null);		
		Converter<TestEnum, TestEnum> converter = binder.createConverter(TestEnum.class, TestEnum.class, emptyValue);
		assertNotNull(converter);
		Result<TestEnum> res = converter.convertToModel(TestEnum.Test1, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(TestEnum.Test1, e));
		assertEquals(TestEnum.Test2, converter.convertToPresentation(TestEnum.Test2, null));
		
		res = converter.convertToModel(emptyValue, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));

		assertEquals(emptyValue, converter.convertToPresentation(null, null));		
	}	
	
}
