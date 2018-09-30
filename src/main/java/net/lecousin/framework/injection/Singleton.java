package net.lecousin.framework.injection;

import java.lang.reflect.Method;

import net.lecousin.framework.application.LCCore;

/** Singleton to be injected. */
public class Singleton extends ObjectProvider {

	/** Constructor. */
	public Singleton(InjectionContext ctx, Class<?> type, Object instance, String id, ObjectMethod destroy) {
		super(ctx, type, id);
		this.instance = instance;
		this.destroy = destroy;
	}
	
	protected Object instance;
	protected ObjectMethod destroy;
	
	@Override
	public Object provide() {
		return instance;
	}
	
	@Override
	public void close() {
		if (instance == null)
			return;
		if (destroy != null)
			try { Injection.call(ctx, instance, destroy); }
			catch (Exception e) {
				LCCore.getApplication().getDefaultLogger().error("Error calling destroy method", e);
			}
		for (Method m : instance.getClass().getMethods())
			if (m.getAnnotation(DestroyMethod.class) != null && m.getParameterCount() == 0)
				try { m.invoke(instance); }
				catch (Throwable t) {
					LCCore.getApplication().getDefaultLogger().error(
						"Error calling method " + m.getName() + " on class " + instance.getClass().getName(), t);
				}

		instance = null;
	}
	
}
