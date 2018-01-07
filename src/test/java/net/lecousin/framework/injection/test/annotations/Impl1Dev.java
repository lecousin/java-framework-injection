package net.lecousin.framework.injection.test.annotations;

import net.lecousin.framework.injection.Injectable;
import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@Injectable(value=Interface1.class, singleton=true)
@InjectableWhen(
	@Property(name="env", value="DEV")
)
public class Impl1Dev implements Interface1 {

	@Override
	public int value() {
		return 1;
	}

}
