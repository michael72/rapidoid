package org.rapidoid.beany;

/*
 * #%L
 * rapidoid-beany
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.util.Cls;
import org.rapidoid.util.TypeKind;
import org.rapidoid.util.U;
import org.rapidoid.var.Var;

@Authors("Nikolche Mihajlovski")
@Since("2.0.0")
public class BeanProp implements Prop {

	private final String name;

	private Field field;

	private Method getter;

	private Method setter;

	private Class<?> declaringType;

	private Class<?> type;
	private Class<?> rawType;

	private TypeKind typeKind;
	private TypeKind rawTypeKind;

	private PropKind propKind = PropKind.NORMAL;

	private Object defaultValue;

	private boolean readOnly = true;

	private ParameterizedType genericType;
	private ParameterizedType rawGenericType;

	public BeanProp(String name) {
		this.name = name;
	}

	public BeanProp(String name, Field field, boolean readOnly) {
		this.name = name;
		this.field = field;
		this.readOnly = readOnly;
	}

	public void init() {
		U.must(field != null || getter != null, "Invalid property: %s", name);

		// TODO: improve inference from getter and setter
		type = rawType = field != null ? field.getType() : getter.getReturnType();
		Type gType = field != null ? field.getGenericType() : getter.getGenericReturnType();
		genericType = rawGenericType = Cls.generic(gType);

		if (Collection.class.isAssignableFrom(type)) {
			readOnly = false;
			propKind = PropKind.COLLECTION;

		} else if (Map.class.isAssignableFrom(type)) {
			readOnly = false;
			propKind = PropKind.MAP;

		} else if (Var.class.isAssignableFrom(type)) {
			U.notNull(genericType, "generic type");

			gType = genericType.getActualTypeArguments()[0];
			genericType = Cls.generic(gType);
			type = Cls.clazz(gType);

			readOnly = false;
			propKind = PropKind.VAR;
		}

		typeKind = Cls.kindOf(type);
		rawTypeKind = Cls.kindOf(rawType);
		declaringType = field != null ? field.getDeclaringClass() : getter.getDeclaringClass();
	}

	public void setGetter(Method getter) {
		this.getter = getter;
	}

	public void setSetter(Method setter) {
		this.setter = setter;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRaw(Object target) {
		// FIXME when target class isn't the property declaring class
		try {
			if (field != null) {
				field.setAccessible(true);
				return (T) field.get(target);
			} else {
				getter.setAccessible(true);
				return (T) getter.invoke(target);
			}
		} catch (Exception e) {
			throw U.rte(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object target) {
		return (T) Beany.unwrap(getRaw(target));
	}

	@Override
	public void set(Object target, Object value) {
		U.must(!isReadOnly(), "Cannot assign value to a read-only property: %s", name);

		// FIXME when target class isn't the property declaring class

		value = Cls.convert(value, getType());

		switch (propKind) {
		case NORMAL:
			normalSet(target, value);
			break;

		case COLLECTION:
			collSet(target, value);
			break;

		case MAP:
			mapSet(target, value);
			break;

		case VAR:
			varSet(target, value);
			break;

		default:
			throw U.notExpected();
		}
	}

	@Override
	public void reset(Object target) {
		U.must(!isReadOnly(), "Cannot reset a read-only property: %s", name);

		// FIXME when target class isn't the property declaring class

		switch (propKind) {
		case NORMAL:
			if (type.isPrimitive()) {
				primitiveReset(target);
			} else {
				normalSet(target, null);
			}
			break;

		case COLLECTION:
			collSet(target, Collections.EMPTY_LIST);
			break;

		case MAP:
			mapSet(target, Collections.EMPTY_MAP);
			break;

		case VAR:
			varSet(target, null);
			break;

		default:
			throw U.notExpected();
		}
	}

	private void primitiveReset(Object target) {
		switch (typeKind) {

		case BOOLEAN:
			normalSet(target, false);
			break;

		case BYTE:
			normalSet(target, (byte) 0);
			break;

		case SHORT:
			normalSet(target, (short) 0);
			break;

		case CHAR:
			normalSet(target, (char) 0);
			break;

		case INT:
			normalSet(target, 0);
			break;

		case LONG:
			normalSet(target, 0L);
			break;

		case FLOAT:
			normalSet(target, (float) 0);
			break;

		case DOUBLE:
			normalSet(target, (double) 0);
			break;

		default:
			throw U.notExpected();
		}

	}

	private void varSet(Object target, Object value) {
		Var<Object> var = getRaw(target);
		var.set(value);
	}

	@SuppressWarnings("unchecked")
	private void collSet(Object target, Object value) {
		U.must(value instanceof Collection<?>, "Expected a collection, but found: %s", value);
		Collection<Object> coll = (Collection<Object>) get(target);

		if (coll == null) {
			coll = (Collection<Object>) Cls.newInstance(type);
		}

		coll.clear();
		coll.addAll((Collection<Object>) value);
	}

	@SuppressWarnings("unchecked")
	private void mapSet(Object target, Object value) {
		U.must(value instanceof Map, "Expected a map, but found: %s", value);
		Map<Object, Object> map = (Map<Object, Object>) get(target);

		if (map == null) {
			map = (Map<Object, Object>) Cls.newInstance(type);
		}

		map.clear();
		map.putAll((Map<Object, Object>) value);
	}

	private void normalSet(Object target, Object value) {
		try {
			if (field != null) {
				field.setAccessible(true);
				field.set(target, value);
			} else if (setter != null) {
				setter.setAccessible(true);
				setter.invoke(target, value);
			} else if (getter != null) {
				throw U.notExpected();
			}
		} catch (Exception e) {
			throw U.rte(e);
		}
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public TypeKind getTypeKind() {
		return typeKind;
	}

	@Override
	public ParameterizedType getGenericType() {
		return genericType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public Class<?> getDeclaringType() {
		return declaringType;
	}

	@Override
	public String toString() {
		return declaringType.getSimpleName() + "#" + name + ":" + type.getSimpleName();
	}

	@Override
	public int getTypeArgsCount() {
		return genericType != null ? genericType.getActualTypeArguments().length : 0;
	}

	@Override
	public Class<?> getTypeArg(int index) {
		U.bounds(index, 0, getTypeArgsCount() - 1);
		return Cls.clazz(genericType.getActualTypeArguments()[index]);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field != null ? field.getAnnotation(annotationClass) : getter.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return field != null ? field.getAnnotations() : getter.getAnnotations();
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@Override
	public TypeKind getRawTypeKind() {
		return rawTypeKind;
	}

	@Override
	public ParameterizedType getRawGenericType() {
		return rawGenericType;
	}

	@Override
	public int getRawTypeArgsCount() {
		return rawGenericType != null ? rawGenericType.getActualTypeArguments().length : 0;
	}

	@Override
	public Class<?> getRawTypeArg(int index) {
		U.bounds(index, 0, getRawTypeArgsCount() - 1);
		return Cls.clazz(rawGenericType.getActualTypeArguments()[index]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((declaringType == null) ? 0 : declaringType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanProp other = (BeanProp) obj;
		if (declaringType == null) {
			if (other.declaringType != null)
				return false;
		} else if (!declaringType.equals(other.declaringType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
