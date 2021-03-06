package net.lecousin.framework.injection;

import java.util.List;

import net.lecousin.framework.io.serialization.SerializationClass;
import net.lecousin.framework.io.serialization.SerializationClass.Attribute;
import net.lecousin.framework.io.serialization.SerializationContext;
import net.lecousin.framework.io.serialization.TypeDefinition;
import net.lecousin.framework.io.serialization.rules.SerializationRule;

/**
 * Serialization rule for dependencies injection:
 * during serialization, injectable objects are not serialized (they will be injected again on deserialization).
 * during deserialization, each time an object is instantiated, its dependencies are injected.
 */
public class InjectionSerializationRule implements SerializationRule {

	/** Constructor. */
	public InjectionSerializationRule(InjectionContext context) {
		this.context = context;
	}
	
	private InjectionContext context;

	@Override
	public boolean apply(SerializationClass type, SerializationContext context, List<SerializationRule> rules, boolean serializing) {
		for (Attribute a : type.getAttributes())
			if (a.getField() != null && a.getField().getAnnotation(Inject.class) != null)
				a.ignore(true);
		return false;
	}
	
	@Override
	public boolean isEquivalent(SerializationRule rule) {
		return rule instanceof InjectionSerializationRule;
	}
	
	@Override
	public void onInstantiation(TypeDefinition type, Object instance, SerializationContext context) throws Exception {
		// do injection on the instance
		Injection.inject(this.context, instance);
	}
	
}
