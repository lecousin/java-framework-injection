package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/** Object value instantiating a given class. */
public class ObjectValueClass implements ObjectValue {

	/** Constructor.
	 * 
	 * @param clazz the class to instantiate
	 * @param ctorParameters parameters of the constructor
	 * @param attributes attributes to inject
	 */
	public ObjectValueClass(Class<?> clazz, List<ObjectValue> ctorParameters, List<ObjectAttribute> attributes) {
		this.clazz = clazz;
		this.ctorParameters = ctorParameters;
		this.attributes = attributes;
	}
	
	private Class<?> clazz;
	private List<ObjectValue> ctorParameters;
	private List<ObjectAttribute> attributes;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		return (T)Injection.create(ctx, clazz, ctorParameters, null, attributes);
	}
	
}
