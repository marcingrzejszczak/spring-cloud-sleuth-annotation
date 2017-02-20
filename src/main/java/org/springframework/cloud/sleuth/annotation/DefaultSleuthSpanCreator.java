package org.springframework.cloud.sleuth.annotation;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

/**
 * Default implementation of the {@link SleuthSpanCreator} that creates
 * a new span around the annotated method.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
class DefaultSleuthSpanCreator implements SleuthSpanCreator {
	
	private final Tracer tracer;
	private final SleuthSpanTagAnnotationHandler annotationSpanUtil;

	public DefaultSleuthSpanCreator(Tracer tracer, SleuthSpanTagAnnotationHandler annotationSpanUtil) {
		this.tracer = tracer;
		this.annotationSpanUtil = annotationSpanUtil;
	}

	@Override
	public Span createSpan(JoinPoint pjp, NewSpan newSpanAnnotation) {
		String key = StringUtils.isNotEmpty(newSpanAnnotation.name()) ?
				newSpanAnnotation.name() : pjp.getSignature().getName();
		this.annotationSpanUtil.addAnnotatedParameters(pjp);
		return createSpan(key);
	}

	private Span createSpan(String key) {
		if (this.tracer.isTracing()) {
			return this.tracer.createSpan(key, this.tracer.getCurrentSpan());
		}
		return this.tracer.createSpan(key);
	}

}
