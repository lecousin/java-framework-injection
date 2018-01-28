package net.lecousin.framework.injection.test.annotations.noscan;

import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@InjectableWhen(@Property(name="env",value="DEV"))
public class TestDev implements ITest {

}
