package net.lecousin.framework.injection;

import java.io.Closeable;

/** Provide objects for injection. */
public abstract class ObjectProvider implements Closeable {

	/** Constructor. */
	public ObjectProvider(InjectionContext ctx, Class<?> type, String id) {
		this.ctx = ctx;
		this.type = type;
		this.id = id;
	}
	
	protected InjectionContext ctx;
	protected Class<?> type;
	protected String id;
	
	/** Provide an object for injection. */
	public abstract Object provide() throws InjectionException;
	
	public Class<?> getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public abstract void close();
	
}
