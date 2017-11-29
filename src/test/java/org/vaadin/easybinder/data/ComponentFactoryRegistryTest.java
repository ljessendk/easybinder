package org.vaadin.easybinder.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.junit.Test;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class ComponentFactoryRegistryTest {
	static enum MyEnum {
		FIRST,
		SECOND;
	}

	public static class MyEntity {
		public String myString;
		public Integer myInteger;
		public int myint;
		public Long myLong;
		public long mylong;
		public Float myFloat;
		public float myfloat;
		public Double myDouble;
		public double mydouble;
		public BigDecimal myBigDecimal;
		public BigInteger myBigInteger;
		public Character myCharacter;
		public char myChar;
		public Boolean myBoolean;
		public boolean myboolean;

		public Date myDate;
		@Temporal(TemporalType.TIMESTAMP)
		public Date myDateTimestamp;
		@Temporal(TemporalType.DATE)
		public Date myDateDate;
		public LocalDate myLocalDate;
		public LocalDateTime myLocalDateTime;
		public MyEnum myEnum;
	}

	public static class MyUnknownType {
	}

	public static class MyNewType {
	}

	MyUnknownType unknownTypeField;
	MyNewType newTypeField;

	ComponentFactoryRegistry cfr = ComponentFactoryRegistry.getInstance();

	@Test
	public void testTypes() throws NoSuchFieldException, SecurityException{
		for(Field field : MyEntity.class.getDeclaredFields()) {
			assertTrue(cfr.createComponent(field).isPresent());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testEnum() throws NoSuchFieldException, SecurityException {
		Optional<Component> result = cfr.createComponent(MyEntity.class.getField("myEnum"));
		assertTrue(result.isPresent());
		assertTrue(result.get() instanceof ComboBox);
		ComboBox<MyEnum> c = (ComboBox<MyEnum>)result.get();
		DataProvider<?, ?> dp = c.getDataProvider();
		Query<?, ?> q = new Query<>(0, 2, null, null, null);
		assertEquals(2, dp.size((Query)q));
	}

	@Test
	public void testGetInstance() {
		assertEquals(cfr, ComponentFactoryRegistry.getInstance());
	}

	@Test
	public void testUnknownType() throws NoSuchFieldException, SecurityException {
		Optional<Component> c = cfr.createComponent(this.getClass().getDeclaredField("unknownTypeField"));
		assertTrue(c.isPresent());
		assertTrue(c.get() instanceof TextField);
	}

	@Test
	public void testNoMatch() throws NoSuchFieldException, SecurityException {
		cfr.addBuildPattern(MyNewType.class, e -> false, e -> mock(Component.class));
		Optional<Component> c = cfr.createComponent(this.getClass().getDeclaredField("newTypeField"));
		assertTrue(c.isPresent());
		assertTrue(c.get() instanceof TextField);
	}

}
