package net.lecousin.framework.injection;

/** Object value taken from an attribute of an existing singleton by its type. */
public class ObjectValueFromSingletonAttributeByType extends ObjectValueFromSingletonAttribute {

	/** Constructor. */
	public ObjectValueFromSingletonAttributeByType(String attributeName, Class<?> type) {
		super(attributeName);
		this.type = type;
	}
	
	private Class<?> type;
	
	@Override
	protected Object getInstance(InjectionContext ctx) throws InjectionException {
		return ctx.getObject(type);
	}
	
}
