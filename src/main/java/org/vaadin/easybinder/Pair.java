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
package org.vaadin.easybinder;

public class Pair<T, V> {
	private final T first;
	private final V second;

	public Pair(T first, V second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Pair<T, V> p = (Pair<T, V>) object;
		return first.equals(p.first) && second.equals(p.second);
	}

	@Override
	public int hashCode() {
		return first.hashCode() + 31 * second.hashCode();
	}
}
