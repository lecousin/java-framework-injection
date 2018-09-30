package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/** Object value created from a string, using Injection#createObjectFromString. */
public class ObjectValueFromString implements ObjectValue {
	
	/** Constructor. */
	public ObjectValueFromString(String value) {
		this.value = value;
	}
	
	private String value;
	
	public boolean isExplicitlyNull() {
		return "null".equals(value);
	}

	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		return Injection.createObjectFromString(type, genericType, value, annotations);
	}
}
