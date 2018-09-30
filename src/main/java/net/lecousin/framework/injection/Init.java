package net.lecousin.framework.injection;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.plugins.CustomExtensionPoint;

/** Initialization. */
public class Init implements CustomExtensionPoint {

	/** Constructor automatically called. */
	public Init() {
		InjectionContext appCtx = new InjectionContext();
		Application app = LCCore.getApplication();
		app.setInstance(InjectionContext.class, appCtx);
		app.toClose(appCtx);
	}
	
	@Override
	public boolean keepAfterInit() {
		return false;
	}
}
