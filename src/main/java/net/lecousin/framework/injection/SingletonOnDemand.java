package net.lecousin.framework.injection;

import net.lecousin.framework.util.Provider;

/** Singleton to be injected. */
public class SingletonOnDemand extends Singleton {

	/** Constructor. */
	public SingletonOnDemand(InjectionContext ctx, Class<?> type, Provider<?> provider, String id, ObjectMethod destroy) {
		super(ctx, type, null, id, destroy);
		this.provider = provider;
	}
	
	protected Provider<?> provider;
	
	@Override
	public Object provide() {
		if (instance == null && provider != null) {
			instance = provider.provide();
			provider = null; // let's garbage collection do its work
		}
		return instance;
	}
	
}
