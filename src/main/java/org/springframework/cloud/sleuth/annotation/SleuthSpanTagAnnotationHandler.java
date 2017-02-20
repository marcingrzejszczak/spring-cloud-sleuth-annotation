package org.springframework.cloud.sleuth.annotation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * This class is able to find all methods annotated with the
 * {@link NewSpan} annotation. All methods mean that if you have both an interface
 * and an implementation annotated with {@link NewSpan} then this class is capable
 * of finding both of them and merging into one set of tracing information.
 *
 * This information is then used to add proper tags to the span from the
 * method arguments that are annotated with {@link SpanTag}.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
class SleuthSpanTagAnnotationHandler {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final ApplicationContext context;
	private final Tracer tracer;
	
	SleuthSpanTagAnnotationHandler(ApplicationContext context, Tracer tracer) {
		this.context = context;
		this.tracer = tracer;
	}

	void addAnnotatedParameters(JoinPoint pjp) {
		try {
			Signature signature = pjp.getStaticPart().getSignature();
			if (signature instanceof MethodSignature) {
				MethodSignature ms = (MethodSignature) signature;
				Method method = ms.getMethod();
				Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method,
						pjp.getTarget().getClass());
				List<SleuthAnnotatedParameter> annotatedParameters =
						findAnnotatedParameters(mostSpecificMethod, pjp.getArgs());
				mergeAnnotatedMethodsIfNecessary(pjp, method, mostSpecificMethod,
						annotatedParameters);
				addAnnotatedArguments(annotatedParameters);
			}
		} catch (SecurityException e) {
			log.error("Exception occurred while trying to add annotated parameters", e);
		}
	}

	private void mergeAnnotatedMethodsIfNecessary(JoinPoint pjp, Method method,
			Method mostSpecificMethod, List<SleuthAnnotatedParameter> annotatedParameters) {
		// that can happen if we have an abstraction and a concrete class that is
		// annotated with @NewSpan annotation
		if (!method.equals(mostSpecificMethod)) {
			List<SleuthAnnotatedParameter> annotatedParametersForActualMethod = findAnnotatedParameters(
					method, pjp.getArgs());
			mergeAnnotatedParameters(annotatedParameters, annotatedParametersForActualMethod);
		}
	}

	private void mergeAnnotatedParameters(List<SleuthAnnotatedParameter> annotatedParametersIndices,
			List<SleuthAnnotatedParameter> annotatedParametersIndicesForActualMethod) {
		for (SleuthAnnotatedParameter container : annotatedParametersIndicesForActualMethod) {
			final int index = container.getParameterIndex();
			boolean parameterContained = false;
			for (SleuthAnnotatedParameter parameterContainer : annotatedParametersIndices) {
				if (parameterContainer.getParameterIndex() == index) {
					parameterContained = true;
					break;
				}
			}
			if (!parameterContained) {
				annotatedParametersIndices.add(container);
			}
		}
	}

	private void addAnnotatedArguments(List<SleuthAnnotatedParameter> toBeAdded) {
		for (SleuthAnnotatedParameter container : toBeAdded) {
			String tagValue = resolveTagValue(container.getAnnotation(), container.getArgument());
			this.tracer.addTag(container.getAnnotation().value(), tagValue);
		}
	}

	String resolveTagValue(SpanTag annotation, Object argument) {
		if (argument == null) {
			return "null";
		}
		if (StringUtils.isNotBlank(annotation.tagValueResolverBeanName())) {
			SleuthTagValueResolver tagValueResolver =
					this.context.getBean(annotation.tagValueResolverBeanName(), SleuthTagValueResolver.class);
			return tagValueResolver.resolveTagValue(argument);
		} else if (StringUtils.isNotBlank(annotation.tagValueExpression())) {
			try {
				ExpressionParser expressionParser = new SpelExpressionParser();
				Expression expression = expressionParser.parseExpression(annotation.tagValueExpression());
				return expression.getValue(argument, String.class);
			} catch (Exception e) {
				log.error("Exception occurred while tying to evaluate the SPEL expression [" + annotation.tagValueExpression() + "]", e);
			}
		}
		return argument.toString();
	}

	private List<SleuthAnnotatedParameter> findAnnotatedParameters(Method method, Object[] args) {
		Annotation[][] parameters = method.getParameterAnnotations();
		List<SleuthAnnotatedParameter> result = new ArrayList<>();
		int i = 0;
		for (Annotation[] parameter : parameters) {
			for (Annotation parameter2 : parameter) {
				if (parameter2 instanceof SpanTag) {
					SpanTag annotation = (SpanTag) parameter2;
					SleuthAnnotatedParameter container = new SleuthAnnotatedParameter();
					container.setAnnotation(annotation);
					container.setArgument(args[i]);
					container.setParameterIndex(i);
					result.add(container);
				}
			}
			i++;
		}
		return result;
	}

}
