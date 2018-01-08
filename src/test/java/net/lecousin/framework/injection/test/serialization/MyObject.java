package net.lecousin.framework.injection.test.serialization;

import net.lecousin.framework.injection.Inject;
import net.lecousin.framework.injection.test.annotations.Interface3;

public class MyObject {

	public int value = 0;
	
	@Inject
	public Interface3 i3;
	
}
