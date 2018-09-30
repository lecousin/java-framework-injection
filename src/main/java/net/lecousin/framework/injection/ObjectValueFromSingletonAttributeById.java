package net.lecousin.framework.injection;

/** Object value taken from an attribute of an existing singleton by ID. */
public class ObjectValueFromSingletonAttributeById extends ObjectValueFromSingletonAttribute {
	
	/** Constructor. */
	public ObjectValueFromSingletonAttributeById(String attributeName, String refId) {
		super(attributeName);
		this.refId = refId;
	}
	
	private String refId;

	@Override
	protected Object getInstance(InjectionContext ctx) throws InjectionException {
		return ctx.getObjectById(refId);
	}
	
}
