package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import net.lecousin.framework.util.ClassUtil;

/** Object value taken from an attribute of an existing singleton. */
public abstract class ObjectValueFromSingletonAttribute implements ObjectValue {
	
	/** Constructor. */
	public ObjectValueFromSingletonAttribute(String attributeName) {
		this.attributeName = attributeName;
	}
	
	protected String attributeName;
	
	protected abstract Object getInstance(InjectionContext ctx) throws InjectionException;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		Object o = getInstance(ctx);
		if (o == null) return null;
		Class<?> cl = o.getClass();
		Field f = ClassUtil.getField(cl, attributeName);
		if (f != null && type.isAssignableFrom(f.getType())) {
			if (!f.isAccessible())
				f.setAccessible(true);
			try { return (T)f.get(o); }
			catch (IllegalAccessException e) {
				throw new InjectionException("Cannot access to attribute " + attributeName + " on class " + cl.getName());
			}
		}
		Method getter = ClassUtil.getGetter(cl, attributeName);
		if (getter != null && (getter.getModifiers() & Modifier.PUBLIC) != 0 && type.isAssignableFrom(getter.getReturnType()))
			try { return (T)getter.invoke(o); }
			catch (IllegalAccessException e) {
				throw new InjectionException("Cannot access to attribute " + attributeName + " on class " + cl.getName());
			} catch (InvocationTargetException e) {
				throw new InjectionException("Error while accessing to attribute " + attributeName
						+ " on class " + cl.getName(), e);
			}
		throw new InjectionException("Attribute " + attributeName + " with type " + type.getName() + " not found on class " + cl.getName());
	}
	
}
