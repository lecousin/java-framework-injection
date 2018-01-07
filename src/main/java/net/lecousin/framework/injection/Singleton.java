package net.lecousin.framework.injection;

/** Singleton to be injected. */
public class Singleton extends ObjectProvider {

	/** Constructor. */
	public Singleton(Class<?> type, Object instance, String id) {
		super(type, id);
		this.instance = instance;
	}
	
	protected Object instance;
	
	@Override
	public Object provide(InjectionContext ctx) {
		return instance;
	}
	
}
