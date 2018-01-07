package net.lecousin.framework.injection;

import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.plugins.CustomExtensionPoint;

/** Initialization. */
public class Init implements CustomExtensionPoint {

	/** Constructor automatically called. */
	public Init() {
		LCCore.getApplication().setInstance(InjectionContext.class, new InjectionContext());
	}
	
	@Override
	public boolean keepAfterInit() {
		return false;
	}
}
