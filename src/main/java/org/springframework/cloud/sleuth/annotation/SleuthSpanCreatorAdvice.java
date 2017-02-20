package org.springframework.cloud.sleuth.annotation;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

/**
 * Due to limitations of Spring AOP we're not creating two separate pointcuts:
 * one for classes annotated with the {@link NewSpan} annotation and one
 * for methods annotated with that annotation cause it will not pick interfaces
 * that have annotated methods.
 *
 * This advice is not registered in Spring context. We will pass it and register
 * it manually when necessary.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
@Aspect
class SleuthSpanCreatorAdvice {

	private final SleuthSpanCreator spanCreator;
	private final Tracer tracer;

	public SleuthSpanCreatorAdvice(SleuthSpanCreator spanCreator, Tracer tracer) {
		this.spanCreator = spanCreator;
		this.tracer = tracer;
	}

	/**
	 * Not creating two separate pointcuts due to the fact that it's not possible
	 * to create a pointcut for interfaces with annotated methods.
	 */
	@Pointcut("execution(public * *(..))")
	private void anyPublicOperation() {
	}

	@Around("anyPublicOperation()")
	public Object instrumentOnMethodAnnotation(ProceedingJoinPoint pjp) throws Throwable {
		Method method = getMethod(pjp);
		if (method == null) {
			return pjp.proceed();
		}
		Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, pjp.getTarget().getClass());
		NewSpan annotation = SleuthAnnotationUtils.findAnnotation(mostSpecificMethod);
		if (annotation == null) {
			return pjp.proceed();
		}
		Span span = null;
		try {
			span = this.spanCreator.createSpan(pjp, annotation);
			return pjp.proceed();
		} finally {
			if (span != null) {
				this.tracer.close(span);
			}
		}
	}
	
	private Method getMethod(ProceedingJoinPoint pjp) {
		Signature signature = pjp.getStaticPart().getSignature();
		if (signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			return methodSignature.getMethod();
		}
		return null;
	}

}
