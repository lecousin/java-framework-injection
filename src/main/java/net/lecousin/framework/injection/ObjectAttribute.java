package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import net.lecousin.framework.util.ClassUtil;

/** Configuration for injecting an object into an attribute. */
public class ObjectAttribute {

	/** Contructor. */
	public ObjectAttribute(String name, String value, String ref, String fromId, String fromAttribute) {
		this.name = name;
		this.value = value;
		this.ref = ref;
		this.fromId = fromId;
		this.fromAttribute = fromAttribute;
	}
	
	/** Contructor. */
	public ObjectAttribute(String name, Class<?> clazz, List<String> parameters, List<ObjectAttribute> attributes) {
		this.name = name;
		this.clazz = clazz;
		this.parameters = parameters;
		this.attributes = attributes;
	}
	
	protected String name;
	protected String value;
	protected String ref;
	protected String fromId;
	protected String fromAttribute;
	
	protected Class<?> clazz;
	protected List<String> parameters;
	protected List<ObjectAttribute> attributes;
	
	public String getName() {
		return name;
	}
	
	/** Create the object based on the given context. */
	public Object create(InjectionContext ctx, Class<?> type, Type genericType, Annotation[] annotations) throws InjectionException {
		if (value != null)
			return Injection.createObjectFromString(type, genericType, value, annotations);
		if (ref != null)
			return ctx.getObjectById(ref, type);
		if (fromId != null) {
			Object o = ctx.getObjectById(fromId);
			if (o == null) return null;
			Class<?> cl = o.getClass();
			Field f = ClassUtil.getField(cl, fromAttribute);
			if (f != null) {
				if (!f.isAccessible())
					f.setAccessible(true);
				try { return f.get(o); }
				catch (IllegalAccessException e) {
					throw new InjectionException("Cannot access to attribute " + fromAttribute + " on class " + cl.getName());
				}
			}
			Method getter = ClassUtil.getGetter(cl, fromAttribute);
			if (getter != null && (getter.getModifiers() & Modifier.PUBLIC) != 0)
				try { return getter.invoke(o); }
				catch (IllegalAccessException e) {
					throw new InjectionException("Cannot access to attribute " + fromAttribute + " on class " + cl.getName());
				} catch (InvocationTargetException e) {
					throw new InjectionException("Error while accessing to attribute " + fromAttribute
							+ " on class " + cl.getName(), e);
				}
		}
		return Injection.create(ctx, clazz, parameters, null, attributes);
	}
	
}
