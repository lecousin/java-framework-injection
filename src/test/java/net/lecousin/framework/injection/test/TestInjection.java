package net.lecousin.framework.injection.test;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;

import org.junit.Assert;
import org.junit.Test;

public class TestInjection extends LCCoreAbstractTest {

	@Test(timeout=120000)
	public void testInjection() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection.xml");
		cfg.blockThrow(0);
		IMySingleton mySingleton = ctx.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonDev);
		Assert.assertEquals("I'm in development", mySingleton.getMyString());
		Assert.assertEquals(1, mySingleton.getMyInteger());
		// TODO continue to check
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection.xml");
		cfg.blockThrow(0);
		mySingleton = ctx.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonProd);
		Assert.assertEquals("I'm in production", mySingleton.getMyString());
		Assert.assertEquals(2, mySingleton.getMyInteger());
		// TODO continue to check
		
		// TODO test @Inject
	}
	
}
