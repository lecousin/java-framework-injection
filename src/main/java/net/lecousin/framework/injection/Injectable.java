package net.lecousin.framework.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the type for injection.
 * By default, if a type implements a single interface or extends a class, this single type is used,
 * else this annotation can be used.
 * Note that this can also be specified in an XML configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Injectable {

	/** Type this class can be injected for. */
	Class<?> value();
	
	/** Indicates if a single instance should be instantiated. */
	boolean singleton() default true;
	
	/** Identifier, or empty if none. */
	String id() default "";
	
}
