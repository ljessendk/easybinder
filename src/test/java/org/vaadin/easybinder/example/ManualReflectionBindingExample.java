package org.vaadin.easybinder.example;

import java.util.EnumSet;

import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.easybinder.ReflectionBinder;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId.LegType;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class ManualReflectionBindingExample extends AbstractTest {

	TextField airline = new TextField("Airline");
	TextField flightNumber = new TextField("Flight number");
	TextField flightSuffix = new TextField("Flight suffix");
	DateField date = new DateField("Date");
	RadioButtonGroup<LegType> legType = new RadioButtonGroup<>("Leg type", EnumSet.allOf(LegType.class));
	DateTimeField sbt = new DateTimeField("SBT");
	DateTimeField ebt = new DateTimeField("EBT");
	DateTimeField abt = new DateTimeField("ABT");
	TextField gate = new TextField("Gate");
	CheckBox canceled = new CheckBox("Canceled");	

	@Override
	public Component getTestComponent() {
		ReflectionBinder<Flight> binder = new ReflectionBinder<>(Flight.class);
		binder.bind(airline, "flightId.airline");
		binder.bind(flightNumber, "flightId.flightNumber");
		binder.bind(flightSuffix, "flightId.flightSuffix");
		binder.bind(date, "flightId.date");
		binder.bind(legType, "flightId.legType");
		binder.bind(sbt, "sbt");
		binder.bind(ebt, "ebt");
		binder.bind(abt, "abt");
		binder.bind(gate, "gate");
		binder.bind(canceled, "canceled");

		FormLayout f = new FormLayout();

		f.addComponents(airline, flightNumber, flightSuffix, date, legType, sbt, ebt, abt, gate, canceled);

		binder.setBean(new Flight());

		return f;
	}
}
