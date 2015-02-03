package org.rapidoid.model;

/*
 * #%L
 * rapidoid-model
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;

@Authors("Nikolche Mihajlovski")
@Since("2.0.0")
public interface Items extends IModel, Iterable<Item> {

	void insert(int index, Item item);

	void add(Item item);

	void addAll(Items items);

	void addAll(List<Item> items);

	void clear();

	Item get(int index);

	boolean isEmpty();

	void remove(int index);

	void set(int index, Item item);

	int size();

	List<Property> properties(String... propertyNames);

	boolean fitsIn(Item item);

	Items range(int fromIndex, int toIndex);

	Items orderedBy(String sortOrder);

}
