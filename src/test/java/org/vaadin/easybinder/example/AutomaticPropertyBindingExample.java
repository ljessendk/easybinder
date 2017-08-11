package org.vaadin.easybinder.example;

import java.util.EnumSet;

import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId.LegType;

import com.vaadin.annotations.PropertyId;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class AutomaticPropertyBindingExample extends AbstractTest {

	@PropertyId("flightId.airline")
	TextField airline = new TextField("Airline");
	@PropertyId("flightId.flightNumber")
	TextField flightNumber = new TextField("Flight number");
	@PropertyId("flightId.flightSuffix")
	TextField flightSuffix = new TextField("Flight suffix");
	@PropertyId("flightId.date")
	DateField date = new DateField("Date");
	@PropertyId("flightId.legType")
	RadioButtonGroup<LegType> legType = new RadioButtonGroup<>("Leg type", EnumSet.allOf(LegType.class));
	DateTimeField sbt = new DateTimeField("SBT");
	DateTimeField ebt = new DateTimeField("EBT");
	DateTimeField abt = new DateTimeField("ABT");
	TextField gate = new TextField("Gate");
	CheckBox canceled = new CheckBox("Canceled");
	
	@Override
	public Component getTestComponent() {
		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);
		binder.bindInstanceFields(this);

		FormLayout f = new FormLayout();

		f.addComponents(airline, flightNumber, flightSuffix, date, legType, sbt, ebt, abt, gate, canceled);

		binder.setBean(new Flight());

		return f;
	}
}
