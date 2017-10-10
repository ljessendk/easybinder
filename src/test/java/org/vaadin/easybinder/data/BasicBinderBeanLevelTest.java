package org.vaadin.easybinder.data;

import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;
import org.vaadin.easybinder.data.BasicBinder;
import org.vaadin.easybinder.data.BinderStatusChangeListener;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class BasicBinderBeanLevelTest {
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = MyValidator.class)
	public static @interface MyEntityValid {
		String message() default "At least one field must be set";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class MyValidator implements ConstraintValidator<MyEntityValid, MyEntity> {

		@Override
		public void initialize(MyEntityValid constraintAnnotation) {
		}

		@Override
		public boolean isValid(MyEntity value, ConstraintValidatorContext context) {
			return value.s1 != null || value.s2 != null;
		}
	}

	@MyEntityValid
	public static class MyEntity {
		String s1;
		String s2;

		public String getS1() {
			return s1;
		}

		public void setS1(String s1) {
			this.s1 = s1;
		}

		public String getS2() {
			return s2;
		}

		public void setS2(String s2) {
			this.s2 = s2;
		}

	}

	ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	Validator validator = factory.getValidator();

	@Test
	public void testBeanClassLevelValidation() {
		TextField field1 = new TextField();
		TextField field2 = new TextField();
		Label statusLabel = new Label();

		BasicBinder<MyEntity> binder = new BasicBinder<>();

		BinderStatusChangeListener statusChangeListener = mock(BinderStatusChangeListener.class);
		binder.addStatusChangeListener(statusChangeListener);
		
		binder.bind(field1, d -> d.getS1() == null ? "" : d.getS1(), (e, f) -> e.setS1("".equals(f) ? null : f), "s1");
		binder.bind(field2, d -> d.getS2() == null ? "" : d.getS2(), (e, f) -> e.setS2("".equals(f) ? null : f), "s2");

		/*
		 * binder.forField(field1) .withNullRepresentation("") .bind("s1");
		 * binder.forField(field2) .withNullRepresentation("") .bind("s2");
		 */
		binder.setStatusLabel(statusLabel);

		reset(statusChangeListener);
		
		MyEntity bean = new MyEntity();

		binder.setBean(bean);

		assertFalse(validator.validate(bean).isEmpty());
		assertFalse(binder.isValid());
		assertEquals("At least one field must be set", statusLabel.getValue());
		
		verify(statusChangeListener, atLeast(1)).statusChange(assertArg(sc -> assertTrue(sc.hasValidationErrors())));

		reset(statusChangeListener);
		
		field2.setValue("Test");

		verify(statusChangeListener, times(1)).statusChange(assertArg(sc -> assertFalse(sc.hasValidationErrors())));
				
		assertTrue(validator.validate(bean).isEmpty());
		assertTrue(binder.isValid());
		assertEquals("", statusLabel.getValue());
		
	}
}
