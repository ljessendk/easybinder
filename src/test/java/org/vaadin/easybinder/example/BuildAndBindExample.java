package org.vaadin.easybinder.example;

import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.easybinder.data.AutoBinder;
import org.vaadin.easybinder.testentity.Flight;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

public class BuildAndBindExample extends AbstractTest {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTestComponent() {
		AutoBinder<Flight> binder = new AutoBinder<>(Flight.class);
		
		FormLayout f = new FormLayout();
		f.addComponents(binder.buildAndBind("flightId"));
		
		Label statusLabel = new Label();
		binder.setStatusLabel(statusLabel);		
		f.addComponents(statusLabel);

		binder.setBean(new Flight());

		return f;
	}
}
