package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.io.serialization.SerializationClass;

/** Map of object values as an object value. */
public class ObjectValueMap implements ObjectValue {

	/** Constructor. */
	public ObjectValueMap(List<ObjectAttribute> elements) {
		this.elements = elements;
	}
	
	private List<ObjectAttribute> elements;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		Map<String, ?> map;
		Class<?> typeOfElement;
		Type genericTypeOfElement;

		if (!type.isInterface() && (type.getModifiers() & Modifier.ABSTRACT) == 0)
			try { map = (Map<String, ?>)type.newInstance(); }
			catch (Exception e) { throw new InjectionException("Unable to instantiate map", e); }
		else
			try { map = (Map<String, ?>)SerializationClass.instantiate(type); }
			catch (Exception e) { throw new InjectionException("Unable to instantiate map", e); }
		
		if (!(genericType instanceof ParameterizedType)) {
			genericTypeOfElement = typeOfElement = Object.class;
		} else {
			genericTypeOfElement = ((ParameterizedType)genericType).getActualTypeArguments()[1];
			if (genericTypeOfElement instanceof ParameterizedType) {
				Type t = ((ParameterizedType)genericTypeOfElement).getRawType();
				if (t instanceof Class) typeOfElement = (Class<?>)t;
				else typeOfElement = Object.class;
			} else if (genericTypeOfElement instanceof Class)
				typeOfElement = (Class<?>)genericTypeOfElement;
			else
				typeOfElement = Object.class;
		}

		for (ObjectAttribute elem : elements)
			((Map)map).put(elem.getName(), elem.getValue().create(ctx, typeOfElement, genericTypeOfElement, new Annotation[0]));
		
		return (T)map;
	}

}
