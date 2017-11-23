package org.vaadin.easybinder.data.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.data.Result;

public class StringLengthConverterValidatorTest {

	StringLengthConverterValidator converter = new StringLengthConverterValidator("Invalid length", 2, 4);

	@Test
	public void testInvalidLengthEmpty() {
		Result<String> res = converter.convertToModel("", null);
		assertTrue(res.isError());
	}

	@Test
	public void testInvalidLengthBelow2() {
		Result<String> res = converter.convertToModel("a", null);
		assertTrue(res.isError());
	}

	@Test
	public void testInvalidLengthAbove4() {
		Result<String> res = converter.convertToModel("abcde", null);
		assertTrue(res.isError());
		res.ifError(e -> e.equals("Invalid length"));
	}

	@Test
	public void testValidLength() {
		Result<String> res = converter.convertToModel("abc", null);
		assertFalse(res.isError());
		res.ifOk(e -> e.equals("abc"));
	}

	@Test
	public void testConvertToPresentation() {
		assertEquals("abcdef", converter.convertToPresentation("abcdef", null));
	}

}
