package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.vaadin.easybinder.AutoBinder;
import org.vaadin.easybinder.BinderAdapter;
import org.vaadin.easybinder.testentity.Flight;
import org.vaadin.easybinder.testentity.FlightId;
import org.vaadin.easybinder.testentity.FlightId.LegType;

import com.vaadin.data.ValidationException;
import com.vaadin.ui.TextField;

public class BinderAdapterTest {
	
	@Test
	public void testReadWriteBean() throws ValidationException {
		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);
		BinderAdapter<Flight> adapter = new BinderAdapter<>(binder, Flight.class); 
		
		binder.buildAndBind("flightId");
		
		Flight f1 = new Flight();
		FlightId id1 = new FlightId();
		id1.setDate(new Date());
		id1.setAirline("XX");
		id1.setFlightNumber(999);
		id1.setLegType(LegType.DEPARTURE);
		f1.setFlightId(id1);
		
		Flight f2 = new Flight();
		
		adapter.readBean(f1);
		
		assertTrue(binder.isValid());
		
		adapter.writeBean(f2);

		assertTrue(f1 != f2);		
		assertEquals(f1.getFlightId().getAirline(), f2.getFlightId().getAirline());
		
		adapter.readBean(f1);
		TextField field = (TextField)binder.getFieldForProperty("flightId.flightNumber").get();
		field.setValue("-1");

		assertFalse(binder.isValid());

		try {
			adapter.writeBean(f2);
			assertTrue(false);
		} catch(ValidationException ex) {
			assertEquals(0, ex.getBeanValidationErrors().size());
			assertEquals(1, ex.getFieldValidationErrors().size());
		}
			
	}
	
}
