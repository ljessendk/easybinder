package org.vaadin.easybinder.unit.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.ZoneId;
import java.util.stream.Stream;

import javax.validation.constraints.Min;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightValid;
import org.vaadin.easybinder.testentity.FlightValidator;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.RequiredFieldConfigurator;
import com.vaadin.data.Result;
import com.vaadin.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.ui.Label;

public class VaadinBeanBinderTest extends BaseTests {

	static BeanValidationBinder<Flight> binder = new BeanValidationBinder<>(Flight.class);

	@BeforeClass
	public static void setup() {
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

		binder.forField(form.airline).bind("flightId.airline");
		binder.forField(form.flightNumber).withConverter(c).bind("flightId.flightNumber");
		binder.forField(form.flightSuffix)
				.withConverter(Converter.from(
						e -> e.length() == 0 ? Result.ok(null)
								: (e.length() == 1 ? Result.ok(e.charAt(0)) : Result.error("Must be 1 character")),
						f -> f == null ? "" : "" + f))
				.bind("flightId.flightSuffix");
		binder.forField(form.date).withConverter(new LocalDateToDateConverter()).bind("flightId.date");
		binder.forField(form.legType).bind("flightId.legType");
		binder.forField(form.sbt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("sbt");
		binder.forField(form.ebt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("ebt");
		binder.forField(form.abt).withConverter(new LocalDateTimeToDateConverter(ZoneId.systemDefault())).bind("abt");
		Binder.Binding<Flight, String> scheduledDependingBinding = binder.forField(form.gate).withNullRepresentation("")
				.withValidator(e -> form.sbt.getValue() == null ? true : e != null, "Gate should be set when scheduled")
				.bind(Flight::getGate, Flight::setGate);
		form.sbt.addValueChangeListener(e -> scheduledDependingBinding.validate());

		binder.bindInstanceFields(form);

		binder.withValidator(e -> new FlightValidator().isValid(e, null), FlightValid.MESSAGE);

		form.flightNumber.setRequiredIndicatorVisible(true);
	}

	@Override
	protected void setBean(Flight flight) {
		binder.setBean(flight);
		binder.validate();
	}

	@Override
	protected Stream<HasValue<?>> getFields() {
		return binder.getFields();
	}

	@Override
	protected boolean isValid() {
		return binder.isValid();
	}

	@Override
	protected void setStatusLabel(Label label) {
		binder.setStatusLabel(label);
	}
		
	@Test
	@Override
	public void testStringConversion() {
		Flight f = new Flight();
		setBean(f);

		// String->String model->presentation
		assertNull(f.getFlightId().getAirline());
		assertEquals("", form.airline.getValue());
		f.getFlightId().setAirline("BB");
		setBean(f);
		assertEquals("BB", form.airline.getValue());
		f.getFlightId().setAirline(null);
		setBean(f);
		assertEquals("", form.airline.getValue());

		// String->String presentation->model
		form.airline.setValue("AA");
		assertEquals("AA", f.getFlightId().getAirline());
		form.airline.setValue("");
		// assertNull(f.getFlightId().getAirline());
		form.airline.setValue("CC");
		assertEquals("CC", f.getFlightId().getAirline());
	}

}
