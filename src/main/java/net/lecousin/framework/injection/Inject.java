package net.lecousin.framework.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field should be injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Inject {

	/** Object id or empty. */
	public String id() default "";
	
	/** True if the object is mandatory. */
	public boolean required() default true;
	
}
