package net.lecousin.framework.injection.tests;

import java.io.FileNotFoundException;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.Injection;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;
import net.lecousin.framework.injection.test.simple.IMySingleton;
import net.lecousin.framework.injection.test.simple.IProvided;
import net.lecousin.framework.injection.test.simple.MySingletonDev;
import net.lecousin.framework.injection.test.simple.MySingletonProd;
import net.lecousin.framework.injection.test.simple.ProvidedDev;
import net.lecousin.framework.injection.test.simple.ProvidedProd;
import net.lecousin.framework.injection.test.simple.TataDev;
import net.lecousin.framework.injection.test.simple.TataProd;
import net.lecousin.framework.injection.test.simple.TitiDev;
import net.lecousin.framework.injection.test.simple.TitiProd;
import net.lecousin.framework.injection.test.simple.TotoDev;
import net.lecousin.framework.injection.test.simple.TotoProd;
import net.lecousin.framework.injection.test.simple.WithDependencies;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInjectionSimple extends LCCoreAbstractTest {

	@BeforeClass
	public static void load() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		ctxDev = app.getInstance(InjectionContext.class);
		ctxDev = new InjectionContext(ctxDev);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctxDev, "test-injection-simple.xml");
		cfg.blockThrow(0);
		// PROD
		app.setProperty("env", "PROD");
		ctxProd = app.getInstance(InjectionContext.class);
		ctxProd = new InjectionContext(ctxProd);
		cfg = InjectionXmlConfiguration.configure(ctxProd, "test-injection-simple.xml");
		cfg.blockThrow(0);
	}
	
	private static InjectionContext ctxDev, ctxProd;

	@Test(timeout=30000)
	public void testBasic() throws Exception {
		Application app = LCCore.getApplication();
		
		app.setProperty("env", "TEST");
		Assert.assertEquals(app.getInstance(InjectionContext.class), ctxDev.getParent());
		ctxDev.getParent().setProperty("test", "true");
		Assert.assertNull(Injection.resolveProperties(ctxDev, app, (String)null));
		Assert.assertEquals("test${env", Injection.resolveProperties(ctxDev, app, "test${env"));
		Assert.assertEquals("test${env2}", Injection.resolveProperties(ctxDev, app, "test${env2}"));
		Assert.assertEquals("testTEST", Injection.resolveProperties(ctxDev, app, "test${env}"));
		Assert.assertEquals("testTESTx", Injection.resolveProperties(ctxDev, app, "test${env}x"));
		Assert.assertEquals("testtruex", Injection.resolveProperties(ctxDev, app, "test${test}x"));
		ctxDev.setParent(null);
		Assert.assertEquals("test${test}x", Injection.resolveProperties(ctxDev, app, "test${test}x"));
		ctxDev.setParent(app.getInstance(InjectionContext.class));
		Assert.assertEquals("testtruex", Injection.resolveProperties(ctxDev, app, "test${test}x"));
		
		Assert.assertNull(ctxDev.getObjectById("xx"));
		Assert.assertNull(ctxDev.getObject(TestInjectionSimple.class));
		
		try {
			InjectionXmlConfiguration.configure(ctxDev, "xx").blockThrow(0);
			throw new AssertionError("FileNotFoundException expected");
		} catch (FileNotFoundException e) {}
	}
	
	@Test(timeout=30000)
	public void testSingleton() throws Exception {
		// DEV
		IMySingleton mySingleton = ctxDev.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonDev);
		Assert.assertEquals("I'm in development", mySingleton.getMyString());
		Assert.assertEquals(1, mySingleton.getMyInteger());
		Assert.assertTrue(mySingleton == ctxDev.getObject(IMySingleton.class));
		
		// PROD
		mySingleton = ctxProd.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonProd);
		Assert.assertEquals("I'm in production", mySingleton.getMyString());
		Assert.assertEquals(2, mySingleton.getMyInteger());
		Assert.assertTrue(mySingleton == ctxProd.getObject(IMySingleton.class));
	}
	
	@Test(timeout=30000)
	public void testFactory() throws Exception {
		// DEV
		IProvided myObject = ctxDev.getObject(IProvided.class);
		Assert.assertTrue(myObject instanceof ProvidedDev);
		Assert.assertEquals("DEV", myObject.getEnv());
		Assert.assertTrue(myObject != ctxDev.getObject(IProvided.class));
		
		// PROD
		myObject = ctxProd.getObject(IProvided.class);
		Assert.assertTrue(myObject instanceof ProvidedProd);
		Assert.assertEquals("PROD", myObject.getEnv());
		Assert.assertTrue(myObject != ctxProd.getObject(IProvided.class));
	}
	
	@Test(timeout=30000)
	public void testSingletonById() throws Exception {
		// DEV
		Object mySingleton = ctxDev.getObjectById("toto");
		Assert.assertTrue(mySingleton instanceof TotoDev);
		TataDev dev = ((TotoDev)mySingleton).getTata();
		Assert.assertEquals("this is the string in dev", dev.getStr());
		Assert.assertEquals(51, dev.getI());
		
		// PROD
		mySingleton = ctxProd.getObjectById("toto");
		Assert.assertTrue(mySingleton instanceof TotoProd);
		TataProd prod = ((TotoProd)mySingleton).getTata();
		Assert.assertEquals("this is the string in prod", prod.getStr());
		Assert.assertEquals(52, prod.getI());
	}
	
	@Test(timeout=30000)
	public void testSingletonWithInjectedAttribute() throws Exception {
		// DEV
		Object mySingleton = ctxDev.getObjectById("titi");
		Assert.assertTrue(mySingleton instanceof TitiDev);
		TitiDev dev = (TitiDev)mySingleton;
		Assert.assertEquals("this is the string in dev", dev.getTata().getStr());
		Assert.assertEquals(51, dev.getTata().getI());
		Assert.assertTrue(dev.getToto() instanceof TotoDev);
		TataDev tataDev = ((TotoDev)dev.getToto()).getTata();
		Assert.assertEquals("this is the string in dev", tataDev.getStr());
		Assert.assertEquals(51, tataDev.getI());

		// PROD
		mySingleton = ctxProd.getObjectById("titi");
		Assert.assertTrue(mySingleton instanceof TitiProd);
		TitiProd prod = (TitiProd)mySingleton;
		Assert.assertEquals("this is the string in prod", prod.getTata().getStr());
		Assert.assertEquals(52, prod.getTata().getI());
		Assert.assertTrue(prod.getToto() instanceof TotoProd);
		TataProd tataProd = ((TotoProd)prod.getToto()).getTata();
		Assert.assertEquals("this is the string in prod", tataProd.getStr());
		Assert.assertEquals(52, tataProd.getI());
		Assert.assertNotNull(prod.provided);
		Assert.assertEquals("TEST", prod.provided.getEnv());
	}

	@Test(timeout=30000)
	public void testInjectAnnotation() throws Exception {
		// DEV
		Object o = ctxDev.getObjectById("with-deps");
		Assert.assertNotNull(o);
		WithDependencies w = (WithDependencies)o;
		Assert.assertTrue(w.mySingleton instanceof MySingletonDev);
		Assert.assertEquals("I'm in development", ((MySingletonDev)w.mySingleton).getMyString());
		Assert.assertEquals(1, ((MySingletonDev)w.mySingleton).getMyInteger());
		Assert.assertTrue(w.provided instanceof ProvidedDev);
		Assert.assertEquals("DEV", ((ProvidedDev)w.provided).getEnv());
		Assert.assertEquals(123, w.init);

		// PROD
		o = ctxProd.getObjectById("with-deps");
		Assert.assertNotNull(o);
		w = (WithDependencies)o;
		Assert.assertTrue(w.mySingleton instanceof MySingletonProd);
		Assert.assertEquals("I'm in production", ((MySingletonProd)w.mySingleton).getMyString());
		Assert.assertEquals(2, ((MySingletonProd)w.mySingleton).getMyInteger());
		Assert.assertTrue(w.provided instanceof ProvidedProd);
		Assert.assertEquals("PROD", ((ProvidedProd)w.provided).getEnv());
		Assert.assertEquals(123, w.init);
	}
	
}
