package org.vaadin.easybinder.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.vaadin.easybinder.data.ConverterRegistry;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;

public class ConverterRegistryTest {
	@Test
	public void testSingleInstance() {
		ConverterRegistry i1 = ConverterRegistry.getInstance();
		ConverterRegistry i2 = ConverterRegistry.getInstance();
		assertTrue(i1 == i2);
	}

	@Test
	public void testRegisterUnregister() {
		ConverterRegistry i = ConverterRegistry.getInstance();

		assertNull(i.getConverter(String.class, String.class));
		
		i.registerConverter(String.class, String.class, Converter.from(e -> Result.ok(e), f -> f));
		
		assertNotNull(i.getConverter(String.class, String.class));
		
		i.unregisterConverter(String.class, String.class);
		
		assertNull(i.getConverter(String.class, String.class));
	}
}
