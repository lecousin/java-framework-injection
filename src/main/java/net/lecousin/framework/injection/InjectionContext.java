package net.lecousin.framework.injection;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.collections.LinkedArrayList;

/** Injection context. */
public class InjectionContext {

	/** Constructor. */
	public InjectionContext(InjectionContext parent) {
		this.parent = parent;
	}

	/** Constructor. */
	public InjectionContext() {
		this(null);
	}

	private InjectionContext parent;
	private Map<String, String> properties = new HashMap<>();
	private LinkedArrayList<ObjectProvider> providers = new LinkedArrayList<>(10);
	private Map<String, ObjectProvider> providerById = new HashMap<>();
	
	public InjectionContext getParent() {
		return parent;
	}
	
	public void setParent(InjectionContext parent) {
		this.parent = parent;
	}
	
	public void setProperty(String name, String value) {
		properties.put(name, value);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	/** Retrieve an object by its id. */
	public Object getObjectById(String id) throws InjectionException {
		ObjectProvider provider = providerById.get(id);
		if (provider == null) {
			if (parent == null)
				return null;
			return parent.getObjectById(id);
		}
		return provider.provide(this);
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
		return (T)provider.provide(this);
	}
	
	/** Retrieve an object by its implemented type. */
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> type) throws InjectionException {
		for (ObjectProvider provider : providers)
			if (type.isAssignableFrom(provider.getType()))
				return (T)provider.provide(this);
		if (parent == null)
			return null;
		return parent.getObject(type);
	}
	
	/** Add an object provider. */
	public void add(ObjectProvider provider) throws InjectionException {
		String id = provider.getId();
		if (id != null) {
			if (providerById.containsKey(id))
				throw new InjectionException("Duplicate object provider with id " + id);
			providerById.put(id,  provider);
		}
		providers.add(provider);
	}
	
}
