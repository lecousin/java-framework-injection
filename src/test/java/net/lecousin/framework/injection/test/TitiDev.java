package net.lecousin.framework.injection.test;

public class TitiDev implements ITiti {

	private IToto toto;
	private TataDev tata;
	
	@Override
	public IToto getToto() {
		return toto;
	}
	
	public TataDev getTata() {
		return tata;
	}
	
}
