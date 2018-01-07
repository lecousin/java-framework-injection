package net.lecousin.framework.injection.test.simple;

import net.lecousin.framework.injection.Inject;

public class WithDependencies {

	@Inject
	public IMySingleton mySingleton;
	
	@Inject
	public IProvided provided;
	
}
