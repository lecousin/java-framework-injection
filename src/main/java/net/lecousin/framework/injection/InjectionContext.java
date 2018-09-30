package net.lecousin.framework.injection;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.collections.LinkedArrayList;

/** Injection context. */
public class InjectionContext implements Closeable {
	
	public static InjectionContext getRoot() {
		return LCCore.getApplication().getInstance(InjectionContext.class);
	}

	/** Constructor. */
	public InjectionContext(InjectionContext parent) {
		this.parent = parent;
		if (parent != null)
			parent.children.add(this);
	}

	/** Constructor. */
	public InjectionContext() {
		this(null);
	}

	private InjectionContext parent;
	private List<InjectionContext> children = new ArrayList<>(2);
	private Map<String, String> properties = new HashMap<>();
	private LinkedArrayList<ObjectProvider> providers = new LinkedArrayList<>(10);
	private Map<String, ObjectProvider> providerById = new HashMap<>();
	
	public InjectionContext getParent() {
		return parent;
	}
	
	/** Set the parent context, and add this context to the children of the given parent. */
	public void setParent(InjectionContext parent) {
		if (this.parent != null)
			this.parent.children.remove(this);
		this.parent = parent;
		if (parent != null)
			parent.children.add(this);
	}
	
	@Override
	public void close() {
		for (InjectionContext ctx : children)
			ctx.close();
		for (ObjectProvider provider : providers)
			provider.close();
	}
	
	/** Set a property. */
	public void setProperty(String name, String value) {
		properties.put(name, value);
	}
	
	/** Return a property or null if not set. */
	public String getProperty(String name) {
		String value = properties.get(name);
		if (value == null && parent != null)
			return parent.getProperty(name);
		return value;
	}
	
	/** Retrieve an object by its id. */
	public Object getObjectById(String id) throws InjectionException {
		ObjectProvider provider = providerById.get(id);
		if (provider == null) {
			if (parent == null)
				return null;
			return parent.getObjectById(id);
		}
		return provider.provide();
	}
	
	/** Retrieve an object by its id and implemented type. */
	@SuppressWarnings("unchecked")
	public <T> T getObjectById(String id, Class<T> type) throws InjectionException {
		ObjectProvider provider = providerById.get(id);
		if (provider == null) {
			if (parent == null)
				return null;
			return parent.getObjectById(id, type);
		}
		if (!type.isAssignableFrom(provider.getType())) {
			if (parent == null)
				return null;
			return parent.getObjectById(id, type);
		}
		return (T)provider.provide();
	}
	
	/** Retrieve an object by its implemented type. */
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> type) throws InjectionException {
		for (ObjectProvider provider : providers)
			if (type.isAssignableFrom(provider.getType()))
				return (T)provider.provide();
		if (parent == null)
			return null;
		return parent.getObject(type);
	}
	
	/** Search an object by ID in this context and its descendents. */
	public Object searchObjectInChildren(String id) throws InjectionException {
		ObjectProvider provider = providerById.get(id);
		if (provider != null)
			return provider.provide();
		for (InjectionContext child : children) {
			Object result = child.searchObjectInChildren(id);
			if (result != null)
				return result;
		}
		return null;
	}
	
	/** Add an object provider. */
	public void add(ObjectProvider provider) throws InjectionException {
		String id = provider.getId();
		if (id != null) {
			if (providerById.containsKey(id))
				throw new InjectionException("Duplicate object provider with id " + id);
			providerById.put(id, provider);
		}
		providers.add(provider);
	}
	
	/** Add a singleton by id, equivalent to call <code>add(new Singleton(type, instance, id))</code>. */
	public void setObject(String id, Class<?> type, Object instance, ObjectMethod destroy) throws InjectionException {
		add(new Singleton(this, type, instance, id, destroy));
	}
	
}
