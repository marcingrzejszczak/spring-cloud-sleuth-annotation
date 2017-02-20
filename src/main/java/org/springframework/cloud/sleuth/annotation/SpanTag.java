package org.springframework.cloud.sleuth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method parameter which is annotated with this annotation,
 * will be added as a tag. The name will be the {@code value} property,
 * using the {@code toString()} representation of the parameter as tag-value.
 * 
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = { ElementType.PARAMETER })
public @interface SpanTag {

	/**
	 * The name of the tag which should be created
	 */
	String value();

	/**
	 * Execute this SPEL expression to calculate the tag value
	 */
	String tagValueExpression() default "";

	/**
	 * Use this bean name to retrieve the tag value
	 */
	String tagValueResolverBeanName() default "";

}
