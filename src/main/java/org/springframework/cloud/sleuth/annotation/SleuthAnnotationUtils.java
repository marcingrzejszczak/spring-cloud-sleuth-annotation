package org.springframework.cloud.sleuth.annotation;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Utility class that can verify whether the method is annotated with
 * the {@link NewSpan} annotation.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
class SleuthAnnotationUtils {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	static boolean isMethodAnnotated(Method method) {
		return findAnnotation(method) != null;
	}

	/**
	 * Searches for an annotation either on a method or inside the method parameters
	 */
	static NewSpan findAnnotation(Method method) {
		NewSpan annotation = AnnotationUtils.findAnnotation(method, NewSpan.class);
		if (annotation == null) {
			try {
				annotation = AnnotationUtils.findAnnotation(method.getDeclaringClass().getMethod(method.getName(), method.getParameterTypes()), NewSpan.class);
			} catch (NoSuchMethodException | SecurityException e) {
				if (log.isDebugEnabled()) {
					log.debug("Exception occurred while tyring to find the annotation", e);
				}
			}
		}
		return annotation;
	}
}
