package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.io.serialization.SerializationClass;

/** List of object values as an object value. */
public class ObjectValueList implements ObjectValue {
	
	/** Constructor. */
	public ObjectValueList(List<ObjectValue> elements) {
		this.elements = elements;
	}
	
	private List<ObjectValue> elements;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		@SuppressWarnings("rawtypes")
		Collection list;
		Class<?> typeOfElement;
		Type genericTypeOfElement;
		if (type.isArray()) {
			list = new LinkedList<>();
			typeOfElement = type.getComponentType();
			genericTypeOfElement = type.getComponentType();
		} else {
			if (!type.isInterface() && (type.getModifiers() & Modifier.ABSTRACT) == 0)
				try { list = (Collection<?>)type.newInstance(); }
				catch (Exception e) { throw new InjectionException("Unable to instantiate collection", e); }
			else
				try { list = (Collection<?>)SerializationClass.instantiate(type); }
				catch (Exception e) { throw new InjectionException("Unable to instantiate collection", e); }
			
			if (!(genericType instanceof ParameterizedType)) {
				genericTypeOfElement = typeOfElement = Object.class;
			} else {
				genericTypeOfElement = ((ParameterizedType)genericType).getActualTypeArguments()[0];
				if (genericTypeOfElement instanceof ParameterizedType) {
					Type t = ((ParameterizedType)genericTypeOfElement).getRawType();
					if (t instanceof Class) typeOfElement = (Class<?>)t;
					else typeOfElement = Object.class;
				} else if (genericTypeOfElement instanceof Class)
					typeOfElement = (Class<?>)genericTypeOfElement;
				else
					typeOfElement = Object.class;
			}
		}

		for (ObjectValue elem : elements)
			list.add(elem.create(ctx, typeOfElement, genericTypeOfElement, new Annotation[0]));
		
		if (type.isArray())
			return (T)list.toArray(ArrayUtil.createGenericArrayOf(list.size(), type.getComponentType()));
		return (T)list;
	}

}
