package org.vaadin.easybinder.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.vaadin.easybinder.data.AutoBinder;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.ui.EGrid;

public class EGridTest {

	@Test
	public void testGrid() {
		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);
		binder.buildAndBind("flightId");
		EGrid<Flight> grid = new EGrid<>(binder, Flight.class);
		assertEquals(10, grid.getColumns().size());

	}
	
}
