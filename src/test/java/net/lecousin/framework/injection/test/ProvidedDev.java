package net.lecousin.framework.injection.test;

public class ProvidedDev implements IProvided {

	private String env;
	
	@Override
	public String getEnv() {
		return env;
	}
	
}
