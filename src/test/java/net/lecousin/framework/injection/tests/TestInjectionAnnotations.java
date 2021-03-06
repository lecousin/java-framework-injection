package net.lecousin.framework.injection.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.Inject;
import net.lecousin.framework.injection.Injection;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.test.annotations.Impl1Dev;
import net.lecousin.framework.injection.test.annotations.Impl1Prod;
import net.lecousin.framework.injection.test.annotations.Impl2Dev;
import net.lecousin.framework.injection.test.annotations.Impl2Prod;
import net.lecousin.framework.injection.test.annotations.Impl3Dev;
import net.lecousin.framework.injection.test.annotations.Impl3Prod;
import net.lecousin.framework.injection.test.annotations.Interface1;
import net.lecousin.framework.injection.test.annotations.Interface2;
import net.lecousin.framework.injection.test.annotations.Interface3;
import net.lecousin.framework.injection.test.annotations.Interface4;
import net.lecousin.framework.injection.test.annotations.WithId1;
import net.lecousin.framework.injection.test.annotations.WithId2;
import net.lecousin.framework.injection.test.annotations.dev.Impl4Dev;
import net.lecousin.framework.injection.test.annotations.noscan.ITest;
import net.lecousin.framework.injection.test.annotations.noscan.TestDev;
import net.lecousin.framework.injection.test.annotations.noscan.TestProd;
import net.lecousin.framework.injection.test.annotations.prod.Impl4Prod;
import net.lecousin.framework.injection.xml.InjectionXmlConfiguration;

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
		
		Impl3Prod i = (Impl3Prod)interf;
		Assert.assertNotNull(i.id1);
		Assert.assertNotNull(i.id2);
		Assert.assertNull(i.id3);
		Assert.assertNull(i.id22);
		Assert.assertTrue(i.id1 instanceof WithId1);
		Assert.assertTrue(i.id2 instanceof WithId2);
		Assert.assertNotEquals(i.id1.value(), ((WithId1)ctxProd.getObjectById("myid1")).value());
		Assert.assertNotEquals(i.id2.value(), ((WithId2)ctxProd.getObjectById("myid2")).value());
	}
	
	@Test(timeout=30000)
	public void test4() throws Exception {
		// DEV
		Interface4 interf = ctxDev.getObject(Interface4.class);
		Assert.assertTrue(interf instanceof Impl4Dev);
		
		// PROD
		interf = ctxProd.getObject(Interface4.class);
		Assert.assertTrue(interf instanceof Impl4Prod);
	}

	@Test(timeout=30000)
	public void test5() throws Exception {
		// DEV
		ITest interf = ctxDev.getObject(ITest.class);
		Assert.assertTrue(interf instanceof TestDev);
		
		// PROD
		interf = ctxProd.getObject(ITest.class);
		Assert.assertTrue(interf instanceof TestProd);
	}

	public static class Test6 {
		@Inject
		public ITest toBeInjected;
		@Inject
		public ITest alreadySet = new TestDev();
	}
	
	@Test(timeout=30000)
	public void test6() throws Exception {
		// DEV
		Test6 t6 = new Test6();
		Injection.inject(ctxDev, t6);
		Assert.assertTrue(t6.toBeInjected instanceof TestDev);
		Assert.assertTrue(t6.alreadySet instanceof TestDev);
		
		// PROD
		t6 = new Test6();
		Injection.inject(ctxProd, t6);
		Assert.assertTrue(t6.toBeInjected instanceof TestProd);
		Assert.assertTrue(t6.alreadySet instanceof TestDev);
	}
}
