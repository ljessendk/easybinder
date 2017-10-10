package org.vaadin.easybinder.example;

import java.util.Date;

import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.EasyGrid;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId;
import org.vaadin.easybinder.testentity.FlightId.LegType;

import com.vaadin.ui.Component;

public class GridExample extends AbstractTest {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTestComponent() {
		Flight flight1 = new Flight();
		FlightId id1 = new FlightId();
		id1.setAirline("XX");
		id1.setFlightNumber(100);
		id1.setFlightSuffix('C');
		id1.setDate(new Date());
		id1.setLegType(LegType.DEPARTURE);
		flight1.setFlightId(id1);
		flight1.setCanceled(false);

		Flight flight2 = new Flight();
		FlightId id2 = new FlightId();
		id2.setAirline("YY");
		id2.setFlightNumber(100);
		id2.setFlightSuffix('C');
		id2.setDate(new Date());
		id2.setLegType(LegType.DEPARTURE);
		flight2.setFlightId(id2);
		flight2.setCanceled(false);

		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);

		binder.buildAndBind("flightId");

		EasyGrid<Flight> grid = new EasyGrid<>(binder);

		grid.setItems(flight1, flight2);

		grid.setWidth("100%");
		grid.getEditor().setEnabled(true);

		return grid;
	}

}
