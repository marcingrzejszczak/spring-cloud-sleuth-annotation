package org.springframework.cloud.sleuth.annotation;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;

class SleuthAnnotationUtils {

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

			}
		}
		return annotation;
	}
}
