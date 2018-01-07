package net.lecousin.framework.injection;

import java.util.List;

/** Factory of objects. */
public class Factory extends ObjectProvider {

	/** Constructor. */
	public Factory(Class<?> baseType, Class<?> implType, ObjectMethod initMethod, List<ObjectAttribute> attributes, String id) {
		super(baseType, id);
		this.implType = implType;
		this.initMethod = initMethod;
		this.attributes = attributes;
	}
	
	protected Class<?> implType;
	protected ObjectMethod initMethod;
	protected List<ObjectAttribute> attributes;
	
	@Override
	public Object provide(InjectionContext ctx) throws InjectionException {
		return Injection.create(ctx, implType, initMethod, attributes);
	}
	
}
