package org.vaadin.easybinder.unit.example;

import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.testentity.Flight;

import com.vaadin.data.HasValue;

public class AutomaticPropertyBindingTest extends BaseTests {

	static AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);

	@BeforeClass
	public static void setup() {
		binder.bindInstanceFields(form);
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

}
