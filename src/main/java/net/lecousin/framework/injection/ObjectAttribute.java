package net.lecousin.framework.injection;

/** Configuration for injecting an object into an attribute. */
public class ObjectAttribute {

	/** Contructor. */
	public ObjectAttribute(String name, ObjectValue value) {
		this.name = name;
		this.value = value;
	}
	
	private String name;
	private ObjectValue value;
	
	public String getName() {
		return name;
	}
	
	public ObjectValue getValue() {
		return value;
	}
	
}
