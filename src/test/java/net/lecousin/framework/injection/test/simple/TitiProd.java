package net.lecousin.framework.injection.test.simple;

public class TitiProd implements ITiti {

	private IToto toto;
	private TataProd tata;
	
	@Override
	public IToto getToto() {
		return toto;
	}
	
	public TataProd getTata() {
		return tata;
	}
	
}
