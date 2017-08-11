package org.vaadin.easybinder.unit.example;

import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId.LegType;

import com.vaadin.data.HasValue;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.RadioButtonGroup;

public class BuildAndBindTest extends BaseTests {

	static AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() {
		binder.buildAndBind("flightId");

		form.airline = (TextField) binder.getFieldForProperty("flightId.airline").get();
		form.flightNumber = (TextField) binder.getFieldForProperty("flightId.flightNumber").get();
		form.flightSuffix = (TextField) binder.getFieldForProperty("flightId.flightSuffix").get();
		form.date = (DateField) binder.getFieldForProperty("flightId.date").get();
		form.legType = (RadioButtonGroup<LegType>) binder.getFieldForProperty("flightId.legType").get();
		form.sbt = (DateTimeField) binder.getFieldForProperty("sbt").get();
		form.ebt = (DateTimeField) binder.getFieldForProperty("ebt").get();
		form.abt = (DateTimeField) binder.getFieldForProperty("abt").get();
		form.gate = (TextField) binder.getFieldForProperty("gate").get();
		form.canceled = (CheckBox) binder.getFieldForProperty("canceled").get();
	}

	@Override
	protected void setBean(Flight flight) {
		binder.setBean(flight);
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
}
