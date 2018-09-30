package net.lecousin.framework.injection.test.simple;

public class TitiDev implements ITiti {

	private IToto toto;
	private IToto toto2;
	private TataDev tata;
	private TataDev tata2;
	
	@Override
	public IToto getToto() {
		return toto;
	}
	
	@Override
	public IToto getToto2() {
		return toto2;
	}
	
	public TataDev getTata() {
		return tata;
	}
	
	public TataDev getTata2() {
		return tata2;
	}
	
}
