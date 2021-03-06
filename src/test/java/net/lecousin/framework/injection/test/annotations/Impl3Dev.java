package net.lecousin.framework.injection.test.annotations;

import net.lecousin.framework.injection.InitMethod;
import net.lecousin.framework.injection.Inject;
import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@InjectableWhen(
	@Property(name="env", value="DEV")
)
public class Impl3Dev implements Interface3 {

	@Inject
	private Interface1 i;
	private boolean init = false;
	
	@InitMethod
	public void init() {
		init = true;
	}
	
	@Override
	public Interface1 getInterface() {
		return i;
	}
	
	@Override
	public int value() {
		return 10;
	}
	
	@Override
	public boolean initialized() {
		return init;
	}
	
}
