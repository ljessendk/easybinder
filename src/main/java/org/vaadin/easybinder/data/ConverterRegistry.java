/*
 * Copyright 2017 Lars Sønderby Jessen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.easybinder.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vaadin.easybinder.data.converters.NullConverter;
import org.vaadin.easybinder.data.converters.StringLengthConverterValidator;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.data.converter.StringToBigIntegerConverter;
import com.vaadin.data.converter.StringToBooleanConverter;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.data.converter.StringToFloatConverter;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.converter.StringToLongConverter;

public class ConverterRegistry {
	static ConverterRegistry instance;

	protected Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> converters = new HashMap<>();

	public static ConverterRegistry getInstance() {
		if (instance == null) {
			instance = new ConverterRegistry();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private ConverterRegistry() {

		registerConverter(String.class, int.class, new StringLengthConverterValidator("Must be a number", 1, null)
				.chain(new StringToIntegerConverter("Must be a number")));
		registerConverter(String.class, Integer.class,
				new NullConverter<String>("").chain(new StringToIntegerConverter("Must be a number")));

		registerConverter(String.class, long.class, new StringLengthConverterValidator("Must be a number", 1, null)
				.chain(new StringToLongConverter("Must be a number")));
		registerConverter(String.class, Long.class,
				new NullConverter<String>("").chain(new StringToLongConverter("Must be a number")));

		registerConverter(String.class, float.class, new StringLengthConverterValidator("Must be a number", 1, null)
				.chain(new StringToFloatConverter("Must be a number")));
		registerConverter(String.class, Float.class,
				new NullConverter<String>("").chain(new StringToFloatConverter("Must be a number")));

		registerConverter(String.class, double.class, new StringLengthConverterValidator("Must be a number", 1, null)
				.chain(new StringToDoubleConverter("Must be a number")));
		registerConverter(String.class, Double.class,
				new NullConverter<String>("").chain(new StringToDoubleConverter("Must be a number")));

		registerConverter(String.class, boolean.class,
				new StringLengthConverterValidator("Must be true or false", 1, null)
						.chain(new StringToBooleanConverter("Must be true or false")));
		registerConverter(String.class, Boolean.class,
				new NullConverter<String>("").chain(new StringToBooleanConverter("Must be true or false")));

		registerConverter(String.class, BigInteger.class,
				new NullConverter<String>("").chain(new StringToBigIntegerConverter("Must be a number")));

		registerConverter(String.class, BigDecimal.class,
				new NullConverter<String>("").chain(new StringToBigDecimalConverter("Must be a number")));

		registerConverter(String.class, BigInteger.class,
				new NullConverter<String>("").chain(new StringToBigIntegerConverter("Must be a number")));

		Converter<String, Character> stringToCharConverter = Converter
				.from(e -> e == null ? Result.ok(null) : Result.ok(e.charAt(0)), f -> f == null ? null : "" + f);

		registerConverter(String.class, char.class,
				new StringLengthConverterValidator("Must be 1 character", 1, 1).chain(stringToCharConverter));
		registerConverter(String.class, Character.class, new NullConverter<String>("")
				.chain(new StringLengthConverterValidator("Must be 1 character", 1, 1)).chain(stringToCharConverter));

		registerConverter(LocalDateTime.class, Date.class, new LocalDateTimeToDateConverter(ZoneId.systemDefault()));
		registerConverter(LocalDate.class, Date.class, new LocalDateToDateConverter());

		registerConverter(Set.class, EnumSet.class, Converter.from(e -> e.isEmpty() ? Result.ok(null) : Result.ok(EnumSet.copyOf(e)), e -> e == null ? new HashSet<Object>() : e));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <PRESENTATIONTYPE, MODELTYPE> Converter<PRESENTATIONTYPE, MODELTYPE> getConverter(
			Class<PRESENTATIONTYPE> presentationType, Class<MODELTYPE> modelType) {
		return (Converter) converters.get(new Pair<>(presentationType, modelType));
	}

	public <PRESENTATIONTYPE, MODELTYPE> void registerConverter(Class<PRESENTATIONTYPE> presentationType,
			Class<MODELTYPE> modelType, Converter<PRESENTATIONTYPE, MODELTYPE> converter) {
		converters.put(new Pair<>(presentationType, modelType), converter);
	}

	public <PRESENTATIONTYPE, MODELTYPE> void unregisterConverter(Class<PRESENTATIONTYPE> presentationType,
			Class<MODELTYPE> modelType) {
		converters.remove(new Pair<>(presentationType, modelType));
	}
}
