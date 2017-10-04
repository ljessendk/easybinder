package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.EasyGrid;
import org.vaadin.easybinder.testentity.Flight;

public class EasyGridTest {

	@Test
	public void testGrid() {
		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);
		binder.buildAndBind("flightId");
		EasyGrid<Flight> grid = new EasyGrid<>(binder, Flight.class);
		assertEquals(10, grid.getColumns().size());

	}
	
}
