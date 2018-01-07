package net.lecousin.framework.injection.tests;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;
import net.lecousin.framework.injection.test.annotations.Impl1Dev;
import net.lecousin.framework.injection.test.annotations.Impl1Prod;
import net.lecousin.framework.injection.test.annotations.Impl2Dev;
import net.lecousin.framework.injection.test.annotations.Impl2Prod;
import net.lecousin.framework.injection.test.annotations.Impl3Dev;
import net.lecousin.framework.injection.test.annotations.Impl3Prod;
import net.lecousin.framework.injection.test.annotations.Interface1;
import net.lecousin.framework.injection.test.annotations.Interface2;
import net.lecousin.framework.injection.test.annotations.Interface3;

import org.junit.Assert;
import org.junit.Test;

public class TestInjectionAnnotations extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void test1() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		Interface1 interf = ctx.getObject(Interface1.class);
		Assert.assertTrue(interf instanceof Impl1Dev);
		Assert.assertEquals(1, interf.value());
		Assert.assertTrue(interf == ctx.getObject(Interface1.class));
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		interf = ctx.getObject(Interface1.class);
		Assert.assertTrue(interf instanceof Impl1Prod);
		Assert.assertEquals(2, interf.value());
		Assert.assertTrue(interf == ctx.getObject(Interface1.class));
	}

	@Test(timeout=30000)
	public void test2() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		Interface2 interf = ctx.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Dev);
		Assert.assertEquals(101, interf.value());
		interf = ctx.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Dev);
		Assert.assertEquals(102, interf.value());
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		interf = ctx.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Prod);
		Assert.assertEquals(201, interf.value());
		interf = ctx.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Prod);
		Assert.assertEquals(202, interf.value());
	}

	@Test(timeout=30000)
	public void test3() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		Interface3 interf = ctx.getObject(Interface3.class);
		Assert.assertTrue(interf instanceof Impl3Dev);
		Assert.assertEquals(10, interf.value());
		Assert.assertEquals(1, interf.getInterface().value());
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		interf = ctx.getObject(Interface3.class);
		Assert.assertTrue(interf instanceof Impl3Prod);
		Assert.assertEquals(20, interf.value());
		Assert.assertEquals(2, interf.getInterface().value());
	}
	
}
