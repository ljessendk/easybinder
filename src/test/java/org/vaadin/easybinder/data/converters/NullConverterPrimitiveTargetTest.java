package org.vaadin.easybinder.data.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.data.Result;

public class NullConverterPrimitiveTargetTest {
	NullConverterPrimitiveTarget<Integer> converter = new NullConverterPrimitiveTarget<>();

	@Test
	public void testToModelNull() {
		Result<Integer> r = converter.convertToModel(null, null);
		assertTrue(r.isError());
	}

	@Test
	public void testToModelNotEmpty() {
		Result<Integer> r = converter.convertToModel(10, null);
		assertFalse(r.isError());
		r.ifOk(e -> assertEquals(new Integer(10), e));
	}

	@Test
	public void testToPresentationNull() {
		assertEquals(null, converter.convertToPresentation(null, null));
	}

	@Test
	public void testToPresentationNotNull() {
		assertEquals(new Integer(11), converter.convertToPresentation(11, null));
	}

}
