package net.lecousin.framework.injection.test.simple;

public class TitiProd implements ITiti {

	private IToto toto;
	private TataProd tata_not_accessible;
	
	public ProvidedProd provided;
	
	@Override
	public IToto getToto() {
		return toto;
	}
	
	public TataProd getTata() {
		return tata_not_accessible;
	}
	
	public void setTata(TataProd t) {
		tata_not_accessible = t;
	}
	
}
