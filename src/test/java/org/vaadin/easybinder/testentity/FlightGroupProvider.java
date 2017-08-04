package org.vaadin.easybinder.testentity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

public class FlightGroupProvider implements DefaultGroupSequenceProvider<Flight> {

	public static interface Scheduled {
	}

	@Override
	public List<Class<?>> getValidationGroups(final Flight flight) {
		final List<Class<?>> classes = new ArrayList<>();

		classes.add(Flight.class);

		if (flight == null) {
			return classes;
		}

		if (flight.getSbt() != null) {
			classes.add(Scheduled.class);
		}

		return classes;
	}
}