package net.lecousin.framework.injection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.io.serialization.SerializationClass;
import net.lecousin.framework.log.Logger;
import net.lecousin.framework.math.IntegerUnit;
import net.lecousin.framework.math.IntegerUnit.UnitConversionException;
import net.lecousin.framework.properties.Property;
import net.lecousin.framework.util.ClassUtil;
import net.lecousin.framework.util.Provider;

/** Utility methods for injection. */
public final class Injection {
	
	private Injection() { /* no instance */ }
	
	/** Create an object to be injected. */
	public static Object create(
		InjectionContext ctx, Class<?> clazz, ObjectMethod initMethod, List<ObjectAttribute> attrs
	) throws InjectionException {
		return create(ctx, clazz, new LinkedList<>(), initMethod, attrs);
	}

	/** Create an object to be injected. */
	public static Object create(InjectionContext ctx, Class<?> clazz, List<String> params, ObjectMethod initMethod, List<ObjectAttribute> attrs)
	throws InjectionException {
		Constructor<?>[] ctors = clazz.getConstructors();
		Object bean = null;
		// search constructor
		for (Constructor<?> ctor : ctors) {
			if ((ctor.getModifiers() & Modifier.PUBLIC) == 0) continue;
			Class<?>[] types = ctor.getParameterTypes();
			if (types.length != params.size()) continue;
			Object[] objects = new Object[types.length];
			Type[] genericTypes = ctor.getGenericParameterTypes();
			Annotation[][] annotations = ctor.getParameterAnnotations();
			for (int i = 0; i < types.length; ++i) {
				try { objects[i] = createObjectFromString(types[i], genericTypes[i], params.get(i), annotations[i]); }
				catch (InjectionException e) {
					objects = null;
					break;
				}
			}
			if (objects == null) continue;
		
			try { bean = ctor.newInstance(objects); }
			catch (IllegalAccessException | InstantiationException e) { /* ignore */ }
			catch (InvocationTargetException e) {
				throw new InjectionException("Error in constructor while instantiating " + clazz.getName(), e);
			}
			break;
		}
		if (bean == null)
			throw new InjectionException("Unable to instantiate " + clazz.getName() + " with given parameters");
		for (ObjectAttribute attr : attrs)
			injectAttribute(ctx, bean, attr);
		inject(ctx, bean);
		for (Method m : clazz.getMethods())
			if (m.getAnnotation(InitMethod.class) != null && m.getParameterCount() == 0)
				try { m.invoke(bean); }
				catch (Throwable t) {
					throw new InjectionException("Error calling method " + m.getName() + " on class " + clazz.getName());
				}
		if (initMethod != null)
			try { call(bean, initMethod); }
			catch (Throwable t) {
				throw new InjectionException("Error calling method " + initMethod.getName() + " on class " + clazz.getName());
			}
		return bean;
	}
	
	/** Create an object from a string value. */
	@SuppressWarnings("unchecked")
	public static Object createObjectFromString(
		Class<?> type, Type genericType, String value, Annotation[] annotations
	) throws InjectionException {
		if ("null".equals(value))
			return null;
		if (String.class.equals(type))
			return value;
		if (boolean.class.equals(type) || Boolean.class.equals(type))
			return "true".equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
		if (long.class.equals(type) || Long.class.equals(type) ||
			int.class.equals(type) || Integer.class.equals(type) ||
			short.class.equals(type) || Short.class.equals(type) ||
			byte.class.equals(type) || Byte.class.equals(type)) {
			value = value.trim();
			int i = 0;
			while (i < value.length() && Character.isDigit(value.charAt(i)))
				i++;
			long val;
			if (i == value.length()) {
				// only digits
				val = Long.parseLong(value);
			} else {
				// unit at the end ?
				Class<? extends IntegerUnit> expectedUnit = null;
				for (Annotation a : annotations)
					if (IntegerUnit.Unit.class.equals(a.annotationType())) {
						expectedUnit = ((IntegerUnit.Unit)a).value();
						break;
					}
				if (expectedUnit == null)
					throw new InjectionException("Invalid value '" + value + "' for type " + type.getName());
				val = Long.parseLong(value.substring(0, i).trim());
				
				String s = value.substring(i).trim().toLowerCase();
				Class<? extends IntegerUnit> unit = IntegerUnit.ParserRegistry.get(s);
				if (unit == null)
					throw new InjectionException("Unknown integer unit " + s + " in value " + value);
				
				try {
					val = IntegerUnit.ConverterRegistry.convert(val, unit, expectedUnit);
				} catch (UnitConversionException e) {
					throw new InjectionException("Cannot convert value", e);
				}
			}
			
			if (long.class.equals(type) || Long.class.equals(type))
				return Long.valueOf(val);
			if (int.class.equals(type) || Integer.class.equals(type)) {
				if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE)
					return Integer.valueOf((int)val);
				throw new InjectionException("Invalid integer value " + val + ": does not fit integer limits");
			}
			if (short.class.equals(type) || Short.class.equals(type)) {
				if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE)
					return Short.valueOf((short)val);
				throw new InjectionException("Invalid short value " + val + ": does not fit short limits");
			}
			if (byte.class.equals(type) || Byte.class.equals(type)) {
				if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE)
					return Byte.valueOf((byte)val);
				throw new InjectionException("Invalid byte value " + val + ": does not fit byte limits");
			}
		}
		
		if (Collection.class.isAssignableFrom(type)) {
			if (!(genericType instanceof ParameterizedType))
				throw new InjectionException("Cannot convert a string into a non-parameterized collection " + type.getName());
			genericType = ((ParameterizedType)genericType).getActualTypeArguments()[0];
			if (genericType instanceof ParameterizedType)
				genericType = ((ParameterizedType)genericType).getRawType();
			if (!(genericType instanceof Class))
				throw new InjectionException("Cannot convert a string into a collection " + type.getName());
			@SuppressWarnings("rawtypes")
			Collection col;
			try { col = (Collection<?>)SerializationClass.instantiate(type); }
			catch (Exception e) {
				throw new InjectionException("Unable to instantiate collection", e);
			}
			String[] values = value.split(",");
			for (String val : values)
				col.add(createObjectFromString((Class<?>)genericType, null, val, annotations));
			return col;
		}
		
		throw new InjectionException("Cannot convert string " + value + " into type " + type.getName());
	}
	
	/** Inject the given attribute in the instance. */
	public static void injectAttribute(InjectionContext ctx, Object instance, ObjectAttribute attribute) throws InjectionException {
		String name = attribute.getName();
		Class<?> cl = instance.getClass();
		Field f = ClassUtil.getField(cl, name);
		if (f != null) {
			if (!f.isAccessible())
				f.setAccessible(true);
			Object o = attribute.create(ctx, f.getType(), f.getGenericType(), f.getAnnotations());
			if (o != null || attribute.isExplicitlyNull())
				try { f.set(instance, o); }
				catch (IllegalAccessException e) {
					throw new InjectionException("Cannot access to attribute " + name + " on class " + cl.getName());
				}
			return;
		}
		Method setter = ClassUtil.getSetter(cl, name);
		if (setter != null && (setter.getModifiers() & Modifier.PUBLIC) != 0) {
			Object o = attribute.create(
				ctx, setter.getParameterTypes()[0], setter.getGenericParameterTypes()[0], setter.getAnnotations());
			if (o != null)
				try { setter.invoke(instance, o); }
				catch (IllegalAccessException e) {
					throw new InjectionException("Cannot call setter of attribute " + name + " on class " + cl.getName());
				}
				catch (InvocationTargetException e) {
					throw new InjectionException("Error while calling the setter for attribute " + name
						+ " on class " + cl.getName(), e);
				}
			return;
		}
		throw new InjectionException("Unknown attribute " + name + " on class " + cl.getName());
	}
	
	/** Perform injection on the instance, by looking for fields having the Inject annotation. */
	public static void inject(InjectionContext ctx, Object instance) throws InjectionException {
		Class<?> cl = instance.getClass();
		for (Field f : ClassUtil.getAllFields(cl)) {
			Inject i = f.getAnnotation(Inject.class);
			if (i == null) continue;
			if (!f.isAccessible())
				f.setAccessible(true);
			try {
				if (f.get(instance) != null)
					continue;
			} catch (IllegalAccessException e) {
				throw new InjectionException("Cannot access to field " + f.getName() + " in class " + cl.getName());
			}
			Object o;
			if (i.id().length() > 0)
				o = ctx.getObjectById(i.id(), f.getType());
			else
				o = ctx.getObject(f.getType());
			if (o == null && i.required())
				throw new InjectionException("Unable to inject field " + f.getName() + " in class " + cl.getName());
			if (o != null)
				try { f.set(instance, o); }
				catch (IllegalAccessException e) {
					throw new InjectionException("Cannot access to field " + f.getName() + " in class " + cl.getName());
				}
		}
	}
	
	/** Call a method on an instance. */
	public static void call(Object instance, ObjectMethod method) throws Exception {
		Method m = ClassUtil.getMethod(instance.getClass(), method.getName(), method.getParameters().size());
		if (m == null) return;
		Class<?>[] types = m.getParameterTypes();
		Object[] objects = new Object[types.length];
		Type[] genericTypes = m.getGenericParameterTypes();
		Annotation[][] annotations = m.getParameterAnnotations();
		for (int i = 0; i < types.length; ++i)
			objects[i] = createObjectFromString(types[i], genericTypes[i], method.getParameters().get(i), annotations[i]);
		m.invoke(instance, objects);
	}
	
	/** Resolve properties in the given value. */
	public static String resolveProperties(InjectionContext ctx, Application app, String value) {
		if (value == null) return null;
		int pos = 0;
		int i;
		while ((i = value.indexOf("${", pos)) >= 0) {
			int j = value.indexOf('}', i + 2);
			if (j < 0) break;
			String name = value.substring(i + 2, j);
			String s = ctx.getProperty(name);
			if (s == null) s = app.getProperty(name);
			if (s != null) {
				value = value.substring(0, i) + s + value.substring(j + 1);
				pos = i + s.length();
				continue;
			}
			pos = j + 1;
		}
		return value;
	}

	/** Resolve properties in the given value. */
	public static String resolveProperties(InjectionContext ctx, Application app, CharSequence value) {
		if (value == null) return null;
		return resolveProperties(ctx, app, value.toString());
	}
	
	/** Scan a package to find injectable objects. */
	public static void scanPackage(InjectionContext ctx, Application app, String pkgName, boolean singletons) throws Exception {
		Logger logger = app.getLoggerFactory().getLogger(Injection.class);
		if (logger.debug())
			logger.debug("Scanning package " + pkgName);
		List<File> files = app.getLibrariesManager().getLibrariesLocations();
		for (File f : files) {
			if (f.isDirectory())
				scanDirectoryPackage(ctx, app, pkgName, singletons, f, logger);
			else
				scanJarPackage(ctx, app, pkgName, singletons, f, logger);
		}
	}
	
	private static void scanDirectoryPackage(
		InjectionContext ctx, Application app, String pkgName, boolean singletons, File classDir, Logger logger
	) throws Exception {
		if (logger.debug())
			logger.debug("Search package " + pkgName + " in library " + classDir.getAbsolutePath());
		String pkgPath = pkgName.replace('.', '/');
		File dir = new File(classDir, pkgPath);
		if (!dir.exists()) return;
		if (logger.debug())
			logger.debug("Package " + pkgName + " found in directory " + dir.getAbsolutePath());
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File f : files) {
			if (f.isDirectory()) continue;
			if (!f.getName().endsWith(".class")) continue;
			String className = f.getName();
			className = className.substring(0, className.length() - 6);
			Class<?> cl = app.getClassLoader().loadClass(pkgName + '.' + className);
			scanClass(ctx, app, singletons, cl, logger);
		}
	}
	
	private static void scanJarPackage(
		InjectionContext ctx, Application app, String pkgName, boolean singletons, File jarFile, Logger logger
	) throws Exception {
		if (logger.debug())
			logger.debug("Search package " + pkgName + " in JAR file " + jarFile.getAbsolutePath());
		String pkgPath = pkgName.replace('.', '/') + '/';
		try (ZipFile jar = new ZipFile(jarFile)) {
			Enumeration<? extends ZipEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				ZipEntry f = entries.nextElement();
				if (f.isDirectory()) continue;
				String name = f.getName();
				if (!name.startsWith(pkgPath)) continue;
				if (!name.endsWith(".class")) continue;
				name = name.substring(0, name.length() - 6);
				name = name.replace('/', '.');
				Class<?> cl = app.getClassLoader().loadClass(name);
				scanClass(ctx, app, singletons, cl, logger);
			}
		}
	}
	
	private static void scanClass(
		InjectionContext ctx, Application app, boolean singleton, Class<?> cl, Logger logger
	) throws Exception {
		if (logger.debug())
			logger.debug("Scan class " + cl.getName());
		InjectableWhen when = cl.getAnnotation(InjectableWhen.class);
		if (when != null) {
			for (Property p : when.value()) {
				String name = p.name();
				String value = p.value();
				value = resolveProperties(ctx, app, value);
				if (!value.equals(app.getProperty(name)))
					return;
			}
		}
		Class<?> type = null;
		String id = "";
		Injectable it = cl.getAnnotation(Injectable.class);
		if (it != null) {
			type = it.value();
			singleton = it.singleton();
			id = it.id();
		} else {
			Class<?>[] interfaces = cl.getInterfaces();
			Class<?> superClass = cl.getSuperclass();
			if (interfaces.length == 1)
				type = interfaces[0];
			else if (superClass != null)
				type = superClass;
			else
				return;
		}
		if (logger.debug())
			logger.debug("Injectable class found: " + cl.getName());
		if (singleton)
			ctx.add(new SingletonOnDemand(type, new Provider<Object>() {
				@Override
				public Object provide() {
					try {
						return create(ctx, cl, null, new ArrayList<>(0));
					} catch (InjectionException e) {
						throw new RuntimeException("Unable to create singleton to be injected", e);
					}
				}
			}, id.length() > 0 ? id : null));
		else
			ctx.add(new Factory(type, cl, null, new ArrayList<>(0), id.length() > 0 ? id : null));
	}
	
}
