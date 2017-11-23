package org.vaadin.easybinder.data.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.vaadin.data.Result;

public class NullConverterTest {
	NullConverter<String> converterNonNullPresentation = new NullConverter<>("");
	NullConverter<String> converterNullPresentation = new NullConverter<>(null);

	@Test
	public void testToModelEmpty() {
		Result<String> r = converterNonNullPresentation.convertToModel("", null);
		assertFalse(r.isError());
		r.ifOk(e -> assertNull(e));
	}

	@Test
	public void testToModelNull() {
		Result<String> r = converterNonNullPresentation.convertToModel(null, null);
		assertFalse(r.isError());
		r.ifOk(e -> assertNull(e));
	}

	@Test
	public void testToModelNotEmpty() {
		Result<String> r = converterNonNullPresentation.convertToModel("giraf", null);
		assertFalse(r.isError());
		r.ifOk(e -> assertEquals("giraf", e));
	}

	@Test
	public void testToPresentationNull() {
		assertEquals("", converterNonNullPresentation.convertToPresentation(null, null));
	}

	@Test
	public void testToPresentationNotNull() {
		assertEquals("test", converterNonNullPresentation.convertToPresentation("test", null));
	}

	@Test
	public void testNullToModelEmpty() {
		Result<String> r = converterNullPresentation.convertToModel(null, null);
		assertFalse(r.isError());
		r.ifOk(e -> assertNull(e));
	}

	@Test
	public void testNullToModelNotEmpty() {
		Result<String> r = converterNullPresentation.convertToModel("giraf", null);
		assertFalse(r.isError());
		r.ifOk(e -> assertEquals("giraf", e));
	}

}
