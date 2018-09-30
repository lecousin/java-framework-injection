package net.lecousin.framework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/** Reference to an object by its ID. */
public class ObjectValueRefId implements ObjectValue {

	/** Constructor. */
	public ObjectValueRefId(String refId) {
		this.refId = refId;
	}
	
	private String refId;
	
	@Override
	public <T> T create(InjectionContext ctx, Class<T> type, Type genericType, Annotation[] annotations)
	throws InjectionException {
		return ctx.getObjectById(refId, type);
	}
	
}
