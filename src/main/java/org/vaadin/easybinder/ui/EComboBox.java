package org.vaadin.easybinder.ui;

import java.util.Collection;

import org.vaadin.easybinder.data.HasGenericType;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;

public class EComboBox<T> extends ComboBox<T> implements HasGenericType<T> {

	private static final long serialVersionUID = 1L;

	protected Class<T> type;

	public EComboBox(Class<T> type, String caption) {
		super(caption);
		this.type = type;
	}

	public EComboBox(Class<T> type, String caption, ListDataProvider<T> dataProvider) {
		super(caption);
		this.type = type;
		setDataProvider(dataProvider);
	}

	public EComboBox(Class<T> type, String caption, Collection<T> items) {
		super(caption);
		this.type = type;
	}

	public EComboBox(Class<T> type) {
		super();
		this.type = type;
	}

	@Override
	public Class<T> getGenericType() {
		return type;
	}

}
