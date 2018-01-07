package net.lecousin.framework.injection;

import net.lecousin.framework.util.Provider;

/** Singleton to be injected. */
public class SingletonOnDemand extends Singleton {

	/** Constructor. */
	public SingletonOnDemand(Class<?> type, Provider<?> provider, String id) {
		super(type, null, id);
		this.provider = provider;
	}
	
	protected Provider<?> provider;
	
	@Override
	public Object provide(InjectionContext ctx) {
		if (instance == null && provider != null) {
			instance = provider.provide();
			provider = null; // let's garbage collection do its work
		}
		return instance;
	}
	
}
