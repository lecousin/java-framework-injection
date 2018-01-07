package net.lecousin.framework.injection;

/** Injection error. */
public class InjectionException extends Exception {

	private static final long serialVersionUID = 2109678670060356195L;

	/** Constructor. */
	public InjectionException(String message) {
		super(message);
	}
	
	/** Constructor. */
	public InjectionException(String message, Throwable cause) {
		super(message + ": " + cause.getMessage(), cause);
	}
	
}
