package net.lecousin.framework.injection.test.simple;

public class TitiProd implements ITiti {

	private IToto toto;
	private IToto toto2;
	private TataProd tata_not_accessible;
	private TataProd tata2_not_accessible;
	
	public ProvidedProd provided;
	
	@Override
	public IToto getToto() {
		return toto;
	}
	
	@Override
	public IToto getToto2() {
		return toto2;
	}
	
	public TataProd getTata() {
		return tata_not_accessible;
	}
	
	public void setTata(TataProd t) {
		tata_not_accessible = t;
	}
	
	public TataProd getTata2() {
		return tata2_not_accessible;
	}
	
	public void setTata2(TataProd t) {
		tata2_not_accessible = t;
	}
	
}
