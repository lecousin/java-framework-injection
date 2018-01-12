package net.lecousin.framework.injection.test.annotations;

import net.lecousin.framework.injection.InitMethod;
import net.lecousin.framework.injection.Inject;
import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@InjectableWhen(
	@Property(name="env", value="PROD")
)
public class Impl3Prod implements Interface3 {

	@Inject
	private Interface1 i;
	@Inject(id="myid1")
	public WithIdAbstract id1;
	@Inject(id="myid2", required=false)
	public WithIdAbstract id2;
	@Inject(id="myid3", required=false)
	public WithIdAbstract id3;
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
		return 20;
	}
	
	@Override
	public boolean initialized() {
		return init;
	}

}
