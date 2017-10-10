package org.vaadin.easybinder.ui;

import org.vaadin.easybinder.data.HasGenericType;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

@SuppressWarnings("serial")
public class EGTypeComponentAdapter<T> extends CustomField<T> implements HasGenericType<T> {

	Class<T> genericType;
	Component component;
	HasValue<T> hasValue;

	T value;

	@SuppressWarnings("unchecked")
	public EGTypeComponentAdapter(Class<T> genericType, Component adaptee) {
		this.genericType = genericType;
		this.component = adaptee;
		hasValue = (HasValue<T>)adaptee;
		hasValue.addValueChangeListener(e -> setValue(e.getValue()));
	}

	@Override
	public Class<T> getGenericType() {
		return genericType;
	}

	@Override
	public T getValue() {
		return hasValue.getValue();
	}

	@Override
	protected Component initContent() {
		return component;
	}

	@Override
	protected void doSetValue(T value) {
		hasValue.setValue(value);
	}

	public Component getEmbeddedComponent() {
		return component;
	}

}
