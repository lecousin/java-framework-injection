package net.lecousin.framework.injection.tests;

import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;

import org.junit.Assert;
import org.junit.Test;

public class TestInjectionImport extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void test() throws Exception {
		InjectionContext ctx = new InjectionContext();
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-import.xml");
		cfg.blockThrow(0);
		Assert.assertEquals("true", ctx.getProperty("importWorks"));
	}
	
}
