package org.springframework.cloud.sleuth.annotation;

import org.aspectj.lang.JoinPoint;
import org.springframework.cloud.sleuth.Span;

/**
 * A contract for creating a new span for a given join point
 * and the {@link NewSpan} annotation.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
public interface SleuthSpanCreator {

	/**
	 * Returns a new {@link Span} for the join point and {@link NewSpan}
	 */
	Span createSpan(JoinPoint pjp, NewSpan sleuthInstrumented);
}
