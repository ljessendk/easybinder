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
package org.vaadin.easybinder.data.converters;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

@SuppressWarnings("serial")
public class NullConverter<T> implements Converter<T, T> {

	protected T nullRepresentation;

	public NullConverter(T nullRepresentation) {
		this.nullRepresentation = nullRepresentation;
	}

	@Override
	public Result<T> convertToModel(T value, ValueContext context) {
		return (nullRepresentation == null && value == null)
				|| (nullRepresentation != null && nullRepresentation.equals(value)) ? Result.ok(null)
						: Result.ok(value);
	}

	@Override
	public T convertToPresentation(T value, ValueContext context) {
		return value == null ? nullRepresentation : value;
	}
}
