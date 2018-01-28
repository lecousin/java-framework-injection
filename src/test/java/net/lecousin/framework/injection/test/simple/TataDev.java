package net.lecousin.framework.injection.test.simple;

public class TataDev {

	@SuppressWarnings("unused")
	private TataDev() {
		// not used
	}
	
	public TataDev(String str, String str2) {
		// not used
	}
	
	public TataDev(String str) {
		// not used
	}
	
	public TataDev(String str, int i) {
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
