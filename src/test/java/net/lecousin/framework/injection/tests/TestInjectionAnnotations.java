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
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInjectionAnnotations extends LCCoreAbstractTest {

	@BeforeClass
	public static void load() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		ctxDev = app.getInstance(InjectionContext.class);
		ctxDev = new InjectionContext(ctxDev);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctxDev, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		// PROD
		app.setProperty("env", "PROD");
		ctxProd = app.getInstance(InjectionContext.class);
		ctxProd = new InjectionContext(ctxProd);
		cfg = InjectionXmlConfiguration.configure(ctxProd, "test-injection-annotations.xml");
		cfg.blockThrow(0);
	}
	
	private static InjectionContext ctxDev, ctxProd;
	
	@Test(timeout=30000)
	public void test1() throws Exception {
		// DEV
		Interface1 interf = ctxDev.getObject(Interface1.class);
		Assert.assertTrue(interf instanceof Impl1Dev);
		Assert.assertEquals(1, interf.value());
		Assert.assertTrue(interf == ctxDev.getObject(Interface1.class));
		
		// PROD
		interf = ctxProd.getObject(Interface1.class);
		Assert.assertTrue(interf instanceof Impl1Prod);
		Assert.assertEquals(2, interf.value());
		Assert.assertTrue(interf == ctxProd.getObject(Interface1.class));
	}

	@Test(timeout=30000)
	public void test2() throws Exception {
		// DEV
		Interface2 interf = ctxDev.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Dev);
		Assert.assertEquals(101, interf.value());
		interf = ctxDev.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Dev);
		Assert.assertEquals(102, interf.value());
		
		// PROD
		interf = ctxProd.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Prod);
		Assert.assertEquals(201, interf.value());
		interf = ctxProd.getObject(Interface2.class);
		Assert.assertTrue(interf instanceof Impl2Prod);
		Assert.assertEquals(202, interf.value());
	}

	@Test(timeout=30000)
	public void test3() throws Exception {
		// DEV
		Interface3 interf = ctxDev.getObject(Interface3.class);
		Assert.assertTrue(interf instanceof Impl3Dev);
		Assert.assertEquals(10, interf.value());
		Assert.assertEquals(1, interf.getInterface().value());
		Assert.assertTrue(interf.initialized());
		Assert.assertTrue(interf == ctxDev.getObject(Interface3.class));
		
		// PROD
		interf = ctxProd.getObject(Interface3.class);
		Assert.assertTrue(interf instanceof Impl3Prod);
		Assert.assertEquals(20, interf.value());
		Assert.assertEquals(2, interf.getInterface().value());
		Assert.assertTrue(interf.initialized());
		Assert.assertTrue(interf == ctxProd.getObject(Interface3.class));
	}
	
}
