package de.koizumi.sleuth.annotation;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;

public class SleuthAnnotationUtils {

	public static boolean isMethodAnnotated(Method method) {
		return findAnnotation(method) != null;
	}
	
	public static NewSpan findAnnotation(Method method) {
		NewSpan annotation = AnnotationUtils.findAnnotation(method, NewSpan.class);
		if (annotation == null) {
			try {
				annotation = AnnotationUtils.findAnnotation(method.getDeclaringClass().getMethod(method.getName(), method.getParameterTypes()), NewSpan.class);
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}
		}
		return annotation;
	}
}
