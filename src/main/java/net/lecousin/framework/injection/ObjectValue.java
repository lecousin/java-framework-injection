package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/** Interface for an object value. */
public interface ObjectValue {
	
	/** Create the object based on the given context. */
	<T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations) throws InjectionException;

}
