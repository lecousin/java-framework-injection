package net.lecousin.framework.injection;

import java.util.ArrayList;
import java.util.List;

/** Represent a method with optional parameters to be called. */
public class ObjectMethod {

	/** Constructor. */
	public ObjectMethod(String name, List<String> parameters) {
		this.name = name;
		if (parameters == null) parameters = new ArrayList<>(0);
		this.parameters = parameters;
	}
	
	protected String name;
	protected List<String> parameters;
	
	public String getName() {
		return name;
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
}
