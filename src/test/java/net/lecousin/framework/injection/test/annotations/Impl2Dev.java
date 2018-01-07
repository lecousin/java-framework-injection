package net.lecousin.framework.injection.test.annotations;

import net.lecousin.framework.injection.Injectable;
import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@Injectable(value=Interface2.class, singleton=false)
@InjectableWhen(
	@Property(name="env", value="DEV")
)
public class Impl2Dev implements Interface2 {

	private static int counter = 1;
	
	@Override
	public int value() {
		return 100 + counter++;
	}

}
