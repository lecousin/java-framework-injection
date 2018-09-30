package net.lecousin.framework.injection.tests;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.xml.InjectionXmlConfiguration;

import org.apache.commons.math3.geometry.VectorFormat;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1DFormat;
import org.junit.Assert;
import org.junit.Test;

public class TestInjectionScanJar extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void test() throws Exception {
		Application app = LCCore.getApplication();
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-scan-jar.xml");
		cfg.blockThrow(0);
		VectorFormat<?> obj = ctx.getObject(VectorFormat.class);
		Assert.assertNotNull(obj);
		Assert.assertEquals(Vector1DFormat.class, obj.getClass());
	}

}
