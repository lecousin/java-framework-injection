package net.lecousin.framework.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.lecousin.framework.properties.Property;

/**
 * This annotation can be used to specify when the annotated class should be used for injection.
 * This may be specified in an XML configuration, but if not, this annotation specify the default one. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectableWhen {

	/** List of properties and values to match. */
	Property[] value();
	
}
