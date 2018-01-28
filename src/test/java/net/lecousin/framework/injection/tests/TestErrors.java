package net.lecousin.framework.injection.tests;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;

import org.junit.Test;

public class TestErrors extends LCCoreAbstractTest {

	@Test(timeout=60000)
	public void test() {
		Application app = LCCore.getApplication();
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		
		testError(ctx, "error/01-empty.xml");
		testError(ctx, "error/02-wrong_root.xml");
		testError(ctx, "error/03-wrong_namespace.xml");
		testError(ctx, "error/04-unknown_element.xml");
		testError(ctx, "error/05-invalid_property1.xml");
		testError(ctx, "error/06-invalid_property2.xml");
	}
	
	private static void testError(InjectionContext ctx, String filename) {
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, filename);
		try {
			cfg.blockThrow(0);
			throw new AssertionError("Error expected");
		} catch (Exception e) {
			// ok
		}
	}
	
}
