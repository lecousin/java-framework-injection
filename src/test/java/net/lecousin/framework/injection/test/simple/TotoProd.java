package net.lecousin.framework.injection.test.simple;

public class TotoProd implements IToto {

	private TataProd tata_not_accessible;
	
	public TataProd getTata() {
		return tata_not_accessible;
	}
	
	public void setTata(TataProd t) {
		tata_not_accessible = t;
	}
	
}
