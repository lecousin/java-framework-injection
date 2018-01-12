package net.lecousin.framework.injection.test.annotations;

import net.lecousin.framework.injection.Injectable;

@Injectable(id="myid1", singleton=false, value=WithIdAbstract.class)
public class WithId1 extends WithIdAbstract {

	private static int counter = 0;
	
	public WithId1() {
		value = ++counter;
	}
	
	private int value;
	
	@Override
	public int value() {
		return value;
	}
	
}
