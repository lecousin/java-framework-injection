package net.lecousin.framework.injection.test.annotations.noscan;

import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.properties.Property;

@InjectableWhen(@Property(name="env",value="PROD"))
public class TestProd implements ITest {

}
