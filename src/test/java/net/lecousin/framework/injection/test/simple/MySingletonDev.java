package net.lecousin.framework.injection.test.simple;

public class MySingletonDev implements IMySingleton {

	private String myString;
	private int myInteger;
	
	public IToto testNull;
	
	@Override
	public String getMyString() {
		return myString;
	}

	@Override
	public void setMyString(String myString) {
		this.myString = myString;
	}

	@Override
	public int getMyInteger() {
		return myInteger;
	}

	@Override
	public void setMyInteger(int myInteger) {
		this.myInteger = myInteger;
	}
	
}
