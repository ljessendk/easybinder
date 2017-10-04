package org.vaadin.easybinder;

import com.vaadin.shared.util.SharedUtil;
import com.vaadin.ui.Grid;

@SuppressWarnings("serial")
public class EasyGrid<BEAN> extends Grid<BEAN> {
		
	public EasyGrid(BasicBinder<BEAN> binder, Class<BEAN> clazz) {
		getEditor().setBinder(new BinderAdapter<BEAN>(binder, clazz));
				
		binder.bindings.stream().forEach(e -> {
			e.getProperty().ifPresent(f -> {
				int beginIndex = f.lastIndexOf('.');
				String propertyName = f.substring(beginIndex == -1 ? 0 : beginIndex+1);
				Column<BEAN, ?> col = addColumn(e.getter).setCaption(SharedUtil.camelCaseToHumanFriendly(propertyName));
				col.setEditorBinding(e);
			});
			
		});		
		
		//getEditor().setBuffered(false);
		
		//getEditor().setEnabled(true);
		
	}
}
