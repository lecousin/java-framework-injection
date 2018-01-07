package net.lecousin.framework.injection;

/** Provide objects for injection. */
public abstract class ObjectProvider {

	/** Constructor. */
	public ObjectProvider(Class<?> type, String id) {
		this.type = type;
		this.id = id;
	}
	
	protected Class<?> type;
	protected String id;
	
	/** Provide an object for injection in the given context. */
	public abstract Object provide(InjectionContext ctx) throws InjectionException;
	
	public Class<?> getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
}
