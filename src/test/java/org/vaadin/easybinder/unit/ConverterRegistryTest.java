package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.vaadin.easybinder.ConverterRegistry;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;

public class ConverterRegistryTest {
	@Test
	public void testConverterRegistry() {
		ConverterRegistry i1 = ConverterRegistry.getInstance();
		ConverterRegistry i2 = ConverterRegistry.getInstance();
		assertTrue(i1 == i2);

		assertNull(i2.getConverter(String.class, String.class));
		
		i2.registerConverter(String.class, String.class, Converter.from(e -> Result.ok(e), f -> f));
		
		assertNotNull(i2.getConverter(String.class, String.class));
		
		i2.unregisterConverter(String.class, String.class);
		
		assertNull(i2.getConverter(String.class, String.class));		
	}
}
