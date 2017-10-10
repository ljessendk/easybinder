package org.vaadin.easybinder;

import org.vaadin.easybinder.data.HasGenericType;

import com.vaadin.shared.util.SharedUtil;
import com.vaadin.ui.Grid;

@SuppressWarnings("serial")
public class EasyGrid<T> extends Grid<T> implements HasGenericType<T> {

	protected Class<T> genericType;

	public EasyGrid(ReflectionBinder<T> binder) {
		this(binder, binder.getGenericType());
	}

	public EasyGrid(BasicBinder<T> binder, Class<T> type) {
		this.genericType = type;
		
		getEditor().setBinder(new BinderAdapter<T>(binder, type));
				
		binder.getBindings().stream().forEach(e -> {
			e.getProperty().ifPresent(f -> {
				int beginIndex = f.lastIndexOf('.');
				String propertyName = f.substring(beginIndex == -1 ? 0 : beginIndex+1);
				Column<T, ?> col = addColumn(e.getGetter()).setCaption(SharedUtil.camelCaseToHumanFriendly(propertyName));
				col.setEditorBinding(e);
			});
			
		});
	}

	@Override
	public Class<T> getGenericType() {
		return genericType;
	}
}
