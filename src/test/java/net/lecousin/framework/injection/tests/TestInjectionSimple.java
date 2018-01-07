package net.lecousin.framework.injection.tests;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
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
import org.junit.Test;

public class TestInjectionSimple extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void testSingleton() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		IMySingleton mySingleton = ctx.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonDev);
		Assert.assertEquals("I'm in development", mySingleton.getMyString());
		Assert.assertEquals(1, mySingleton.getMyInteger());
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		mySingleton = ctx.getObject(IMySingleton.class);
		Assert.assertTrue(mySingleton instanceof MySingletonProd);
		Assert.assertEquals("I'm in production", mySingleton.getMyString());
		Assert.assertEquals(2, mySingleton.getMyInteger());
	}
	
	@Test(timeout=30000)
	public void testFactory() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		IProvided myObject = ctx.getObject(IProvided.class);
		Assert.assertTrue(myObject instanceof ProvidedDev);
		Assert.assertEquals("DEV", myObject.getEnv());
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		myObject = ctx.getObject(IProvided.class);
		Assert.assertTrue(myObject instanceof ProvidedProd);
		Assert.assertEquals("PROD", myObject.getEnv());
	}
	
	@Test(timeout=30000)
	public void testSingletonById() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		Object mySingleton = ctx.getObjectById("toto");
		Assert.assertTrue(mySingleton instanceof TotoDev);
		TataDev dev = ((TotoDev)mySingleton).getTata();
		Assert.assertEquals("this is the string in dev", dev.getStr());
		Assert.assertEquals(51, dev.getI());
		
		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		mySingleton = ctx.getObjectById("toto");
		Assert.assertTrue(mySingleton instanceof TotoProd);
		TataProd prod = ((TotoProd)mySingleton).getTata();
		Assert.assertEquals("this is the string in prod", prod.getStr());
		Assert.assertEquals(52, prod.getI());
	}
	
	@Test(timeout=30000)
	public void testSingletonWithIjectedAttribute() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		Object mySingleton = ctx.getObjectById("titi");
		Assert.assertTrue(mySingleton instanceof TitiDev);
		TitiDev dev = (TitiDev)mySingleton;
		Assert.assertEquals("this is the string in dev", dev.getTata().getStr());
		Assert.assertEquals(51, dev.getTata().getI());
		Assert.assertTrue(dev.getToto() instanceof TotoDev);
		TataDev tataDev = ((TotoDev)dev.getToto()).getTata();
		Assert.assertEquals("this is the string in dev", tataDev.getStr());
		Assert.assertEquals(51, tataDev.getI());

		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		mySingleton = ctx.getObjectById("titi");
		Assert.assertTrue(mySingleton instanceof TitiProd);
		TitiProd prod = (TitiProd)mySingleton;
		Assert.assertEquals("this is the string in prod", prod.getTata().getStr());
		Assert.assertEquals(52, prod.getTata().getI());
		Assert.assertTrue(prod.getToto() instanceof TotoProd);
		TataProd tataProd = ((TotoProd)prod.getToto()).getTata();
		Assert.assertEquals("this is the string in prod", tataProd.getStr());
		Assert.assertEquals(52, tataProd.getI());
	}

	@Test(timeout=30000)
	public void testInjectAnnotation() throws Exception {
		Application app = LCCore.getApplication();
		// DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		Object o = ctx.getObjectById("with-deps");
		Assert.assertNotNull(o);
		WithDependencies w = (WithDependencies)o;
		Assert.assertTrue(w.mySingleton instanceof MySingletonDev);
		Assert.assertEquals("I'm in development", ((MySingletonDev)w.mySingleton).getMyString());
		Assert.assertEquals(1, ((MySingletonDev)w.mySingleton).getMyInteger());
		Assert.assertTrue(w.provided instanceof ProvidedDev);
		Assert.assertEquals("DEV", ((ProvidedDev)w.provided).getEnv());

		// PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-simple.xml");
		cfg.blockThrow(0);
		o = ctx.getObjectById("with-deps");
		Assert.assertNotNull(o);
		w = (WithDependencies)o;
		Assert.assertTrue(w.mySingleton instanceof MySingletonProd);
		Assert.assertEquals("I'm in production", ((MySingletonProd)w.mySingleton).getMyString());
		Assert.assertEquals(2, ((MySingletonProd)w.mySingleton).getMyInteger());
		Assert.assertTrue(w.provided instanceof ProvidedProd);
		Assert.assertEquals("PROD", ((ProvidedProd)w.provided).getEnv());
	}
	
}
