package org.vaadin.easybinder.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.junit.Test;
import org.vaadin.easybinder.BasicBinder;
import org.vaadin.easybinder.NullConverter;

import com.vaadin.ui.TextField;

public class BasicBinderTest {
	
	public class MyEntity {
		@NotNull
		String firstName;
		String lastName;
		
		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
	
	public class MyForm {
		TextField firstName = new TextField();
		TextField lastName = new TextField();
	}
	
	@Test
	public void testBindUnbind() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName");
		
		assertEquals(2, binder.getFields().collect(Collectors.toList()).size());

		assertEquals(form.firstName, binder.getFieldForProperty("firstName").get());
		assertEquals(form.lastName, binder.getFieldForProperty("lastName").get());
		
		binder.unbind(form.firstName);
		
		assertEquals(1, binder.getFields().collect(Collectors.toList()).size());

		assertFalse(binder.getFieldForProperty("firstName").isPresent());
		assertEquals(form.lastName, binder.getFieldForProperty("lastName").get());
		
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName");

		assertEquals(2, binder.getFields().collect(Collectors.toList()).size());
		
		binder.unbind();
		
		assertFalse(binder.getFieldForProperty("firstName").isPresent());
		assertFalse(binder.getFieldForProperty("lastName").isPresent());		
	}

	@Test
	public void testSetBeanBeforeBind() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		MyEntity entity = new MyEntity();
		binder.setBean(entity);
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));

		assertFalse(binder.isValid());	
	}	
	
	@Test
	public void testNoPropertyProvided() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		MyEntity entity = new MyEntity();
		binder.setBean(entity);
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), null, new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, null, new NullConverter<>(""));

		assertFalse(binder.isValid());			
	}
	
	boolean isStatusChanged = false;
	boolean isValueChanged = false;
	boolean isHasValidationErrors = false;
	
	@Test
	public void testAssignUnassign() {
		MyForm form = new MyForm();
		BasicBinder<MyEntity> binder = new BasicBinder<>();
		binder.addStatusChangeListener(e -> {isHasValidationErrors = e.hasValidationErrors(); isStatusChanged = true;});
		binder.addValueChangeListener(e -> isValueChanged = true);
		
		assertFalse(isStatusChanged);
		
		binder.bind(form.firstName, e -> e.getFirstName(), (e,f) -> e.setFirstName(f), "firstName", new NullConverter<>(""));
		binder.bind(form.lastName, MyEntity::getLastName, MyEntity::setLastName, "lastName", new NullConverter<>(""));
		
		assertTrue(isStatusChanged);
		isStatusChanged = false;
		assertFalse(isValueChanged);
		
		MyEntity e = new MyEntity();
				
		binder.setBean(e);

		assertFalse(binder.isValid());
		assertFalse(binder.getHasChanges());
		
		assertTrue(isStatusChanged);
		assertTrue(isHasValidationErrors);
		isStatusChanged = false;
		assertFalse(isValueChanged);
		
		form.lastName.setValue("giraf");
		
		assertTrue(isValueChanged);
		assertTrue(isStatusChanged);
		assertTrue(isHasValidationErrors);		
		assertTrue(binder.getHasChanges());
		isValueChanged = false;
		isStatusChanged = false;
		
		form.firstName.setValue("giraf");
		
		assertTrue(isValueChanged);
		assertTrue(isStatusChanged);
		assertFalse(isHasValidationErrors);		
		assertTrue(binder.getHasChanges());
		
		binder.removeBean();
	}
	

}
