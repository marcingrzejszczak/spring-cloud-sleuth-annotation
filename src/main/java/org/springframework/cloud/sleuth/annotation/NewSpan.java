package org.springframework.cloud.sleuth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to create a new span around a public method or a class.
 *
 * For each public method in an annotated class, or self annotated method,
 * a new {@link org.springframework.cloud.sleuth.Span} will be created.
 * Method parameters can be annotated with {@link SpanTag}, which will
 * in adding the parameter value as a tag to the span.
 * 
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface NewSpan {

	/**
	 * The name of the span which will be created. Default is "methodname"
	 */
	String name() default "";
}
