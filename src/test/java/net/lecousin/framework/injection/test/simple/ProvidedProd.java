package net.lecousin.framework.injection.test.simple;

public class ProvidedProd implements IProvided {

	private String env;
	
	@Override
	public String getEnv() {
		return env;
	}
	
}
