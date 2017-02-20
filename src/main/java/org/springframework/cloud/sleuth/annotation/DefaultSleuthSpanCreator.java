package org.springframework.cloud.sleuth.annotation;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

/**
 *
 * @author Christian Schwerdtfeger
 *
 * @since 1.2.0
 */
class DefaultSleuthSpanCreator implements SleuthSpanCreator {
	
	private Tracer tracer;
	private SleuthSpanTagAnnotationHandler annotationSpanUtil;

	@Autowired
	public DefaultSleuthSpanCreator(Tracer tracer, SleuthSpanTagAnnotationHandler annotationSpanUtil) {
		this.tracer = tracer;
		this.annotationSpanUtil = annotationSpanUtil;
	}

	@Override
	public Span createSpan(JoinPoint pjp, NewSpan newSpanAnnotation) {
		if (tracer.isTracing()) {
			String key = StringUtils.isNotEmpty(newSpanAnnotation.name()) ?
					newSpanAnnotation.name() : pjp.getSignature().getDeclaringType().getSimpleName() + "/" + pjp.getSignature().getName();
			Span span = tracer.createSpan(key, tracer.getCurrentSpan());
			this.annotationSpanUtil.addAnnotatedParameters(pjp);
			return span;
		}
		return null;
	}

}
