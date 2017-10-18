package org.vaadin.easybinder.data.converters;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

@SuppressWarnings("serial")
public class NullConverterPrimitiveTarget<T> implements Converter<T, T> {

	protected T nullRepresentation;

	public NullConverterPrimitiveTarget(T nullRepresentation) {
		this.nullRepresentation = nullRepresentation;
	}

	@Override
	public Result<T> convertToModel(T value, ValueContext context) {
		return value == null ? Result.error("Null not allowed") : Result.ok(value);
	}

	@Override
	public T convertToPresentation(T value, ValueContext context) {
		return value == null ? nullRepresentation : value;
	}
}
