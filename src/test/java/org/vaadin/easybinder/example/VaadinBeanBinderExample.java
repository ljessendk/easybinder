package org.vaadin.easybinder.example;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Converter;
import com.vaadin.data.RequiredFieldConfigurator;
import com.vaadin.data.Result;
import com.vaadin.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;

import java.time.ZoneId;
import java.util.EnumSet;

import javax.validation.constraints.Min;

import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightValid;
import org.vaadin.easybinder.testentity.FlightValidator;
import org.vaadin.easybinder.testentity.FlightId.LegType;

public class VaadinBeanBinderExample extends AbstractTest {
	private static final long serialVersionUID = 1L;

	TextField airline = new TextField("Airline");
	TextField flightNumber = new TextField("Flight number");
	TextField flightSuffix = new TextField("Flight suffix");
	DateField date = new DateField("Date");
	RadioButtonGroup<LegType> legType = new RadioButtonGroup<>("Leg type", EnumSet.allOf(LegType.class));
	DateTimeField sbt = new DateTimeField("SBT");
	DateTimeField ebt = new DateTimeField("EBT");
	DateTimeField abt = new DateTimeField("ABT");
	TextField gate = new TextField("Gate");

	@Override
	public Component getTestComponent() {
		BeanValidationBinder<Flight> binder = new BeanValidationBinder<>(Flight.class);

		RequiredFieldConfigurator MIN = annotation -> annotation.annotationType().equals(Min.class)
				&& ((Min) annotation).value() > 0;

		binder.setRequiredConfigurator(MIN.chain(RequiredFieldConfigurator.DEFAULT));

		Converter<String, Integer> c = Converter.from(e -> {
			if (e.length() == 0) {
				return Result.error("Must be a number");
			}
			try {
				return Result.ok(Integer.parseInt(e));
			} catch (NumberFormatException ex) {
				return Result.error("Must be a number");
			}
		}, e -> Integer.toString(e));

		binder.forField(airline).bind("flightId.airline");
		binder.forField(flightNumber).withConverter(c).bind("flightId.flightNumber");
		binder.forField(flightSuffix)
				.withConverter(Converter.from(
						e -> e.length() == 0 ? Result.ok(null)
								: (e.length() == 1 ? Result.ok(e.charAt(0)) : Result.error("Must be 1 character")),
						f -> f == null ? "" : "" + f))
				.bind("flightId.flightSuffix");
		binder.forField(date).withConverter(new LocalDateToDateConverter()).bind("flightId.date");
		binder.forField(legType).bind("flightId.legType");
		binder.forField(sbt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("sbt");
		binder.forField(ebt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("ebt");
		binder.forField(abt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("abt");
		Binder.Binding<Flight, String> scheduledDependingBinding = binder.forField(gate).withNullRepresentation("")
				.withValidator(e -> sbt.getValue() == null ? true : e != null, "Gate should be set when scheduled")
				.bind(Flight::getGate, Flight::setGate);
		sbt.addValueChangeListener(e -> scheduledDependingBinding.validate());

		binder.bindInstanceFields(this);

		binder.withValidator(e -> new FlightValidator().isValid(e, null), FlightValid.MESSAGE);

		flightNumber.setRequiredIndicatorVisible(true);

		FormLayout f = new FormLayout();

		f.addComponents(airline, flightNumber, flightSuffix, date, legType, sbt, ebt, abt, gate);

		binder.setBean(new Flight());

		return f;
	}

}
