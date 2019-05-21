package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.vaadin.easybinder.data.ConverterRegistry;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;

public class ConverterRegistryTest {
	ConverterRegistry r = ConverterRegistry.getInstance();


	@Test
	public void testSingleInstance() {
		ConverterRegistry r2 = ConverterRegistry.getInstance();
		assertEquals(r, r2);
	}

	@Test
	public void testRegisterUnregister() {
		assertNull(r.getConverter(String.class, String.class));

		r.registerConverter(String.class, String.class, Converter.from(e -> Result.ok(e), f -> f));

		assertNotNull(r.getConverter(String.class, String.class));

		r.unregisterConverter(String.class, String.class);

		assertNull(r.getConverter(String.class, String.class));
	}

	@Test
	public void testStringToCharacterConverterGetConverter() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		assertNotNull(c);
	}

	@Test
	public void testStringToCharacterConverterConvertToModelCharaterInput() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		Result<Character> res = c.convertToModel("C", null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(Character.valueOf('C'), e));
	}

	@Test
	public void testStringToCharacterConverterConvertToModelEmptyInput() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		Result<Character> res = c.convertToModel("", null);
		assertFalse(res.isError());
		res.ifOk(e -> assertEquals(null, e));
	}

	@Test
	public void testStringToCharacterConverterConvertToModelTooManyChars() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		Result<Character> res = c.convertToModel("CC", null);
		assertTrue(res.isError());
	}

	@Test
	public void testStringToCharacterConverterConvertToModelNullInput() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		Result<Character> res = c.convertToModel(null, null);
		assertFalse(res.isError());
		res.ifOk(e -> assertNull(e));
	}

	@Test
	public void testStringtoCharacterConverterConvertToPresentation() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		assertEquals("B", c.convertToPresentation('B', null));
	}

	@Test
	public void testStringtoCharacterConverterConvertToPresentationNull() {
		Converter<String, Character> c = r.getConverter(String.class, Character.class);
		assertEquals("", c.convertToPresentation(null, null));
	}

	@Test
	public void testSetToEnumSetGetConverter() {
		@SuppressWarnings("rawtypes")
		Converter<Set, EnumSet> c = r.getConverter(Set.class, EnumSet.class);
		assertNotNull(c);
	}

	public static enum TestEnum {
		Test1,
		Test2;
	}

	@Test
	public void testSetToEnumSetConverterConvertToModelEmptySet() {
		@SuppressWarnings("rawtypes")
		Converter<Set, EnumSet> c = r.getConverter(Set.class, EnumSet.class);
		@SuppressWarnings("rawtypes")
		Result<EnumSet> esetr = c.convertToModel(new HashSet<TestEnum>(), null);
		assertFalse(esetr.isError());
		esetr.ifOk(e -> assertNull(e));
	}

	@Test
	public void testSetToEnumSetConverterConvertToModelNonEmptySet() {
		@SuppressWarnings("rawtypes")
		Converter<Set, EnumSet> c = r.getConverter(Set.class, EnumSet.class);
		HashSet<TestEnum> mySet = new HashSet<TestEnum>(Arrays.asList(TestEnum.Test1));
		@SuppressWarnings("rawtypes")
		Result<EnumSet> esetr = c.convertToModel(mySet, null);
		assertFalse(esetr.isError());
		esetr.ifOk(e -> {
			assertTrue(e.contains(TestEnum.Test1));
			assertFalse(e.contains(TestEnum.Test2));
		});
	}

	@Test
	public void testSetToEnumSetConverterConvertToPresentationEmptySet() {
		@SuppressWarnings("rawtypes")
		Converter<Set, EnumSet> c = r.getConverter(Set.class, EnumSet.class);
		assertTrue(c.convertToPresentation(null, null).isEmpty());
	}

	@Test
	public void testSetToEnumSetConverterConvertToPresentationNonEmptySet() {
		@SuppressWarnings("rawtypes")
		Converter<Set, EnumSet> c = r.getConverter(Set.class, EnumSet.class);
		assertEquals(1, c.convertToPresentation(EnumSet.of(TestEnum.Test1), null).size());
	}

}
