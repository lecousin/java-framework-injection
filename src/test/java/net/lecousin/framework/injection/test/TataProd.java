package net.lecousin.framework.injection.test;

public class TataProd {

	public TataProd(String str, int i) {
		this.str = str;
		this.i = i;
	}
	
	private String str;
	private int i;

	public String getStr() {
		return str;
	}

	public int getI() {
		return i;
	}
	
}
