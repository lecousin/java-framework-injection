package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/** Reference to an object by its type. */
public class ObjectValueRefType implements ObjectValue {

	/** Constructor. */
	public ObjectValueRefType(Class<?> type) {
		this.type = type;
	}
	
	private Class<?> type;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		return (T)ctx.getObject(this.type);
	}

}
