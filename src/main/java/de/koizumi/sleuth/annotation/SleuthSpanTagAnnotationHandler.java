package de.koizumi.sleuth.annotation;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SleuthSpanTagAnnotationHandler {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private ApplicationContext context;

	private Tracer tracer;

	@Autowired
	public SleuthSpanTagAnnotationHandler(ApplicationContext context, Tracer tracer) {
		this.context = context;
		this.tracer = tracer;
	}

	public void addAnnotatedParameters(JoinPoint pjp) {
		try {
			Signature signature = pjp.getStaticPart().getSignature();
			if (signature instanceof MethodSignature) {
				MethodSignature ms = (MethodSignature) signature;
				Method method = ms.getMethod();
				Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, pjp.getTarget().getClass());
				List<SleuthAnnotatedParameterContainer> annotatedParametersIndices = findAnnotatedParameters(
						mostSpecificMethod, pjp.getArgs());
				if (!method.equals(mostSpecificMethod)) {
					List<SleuthAnnotatedParameterContainer> annotatedParametersIndicesForActualMethod = findAnnotatedParameters(
							method, pjp.getArgs());
					mergeAnnotatedParameterContainers(annotatedParametersIndices, annotatedParametersIndicesForActualMethod);
				}
				addAnnotatedArguments(annotatedParametersIndices);
			}
		} catch (SecurityException e) {
			log.error("Exception occurred while trying to add annotated parameters, e");
		}
	}

	private void mergeAnnotatedParameterContainers(List<SleuthAnnotatedParameterContainer> annotatedParametersIndices,
			List<SleuthAnnotatedParameterContainer> annotatedParametersIndicesForActualMethod) {
		for (SleuthAnnotatedParameterContainer container : annotatedParametersIndicesForActualMethod) {
			final int index = container.getParameterIndex();
			boolean parameterContained = false;
			for (SleuthAnnotatedParameterContainer parameterContainer : annotatedParametersIndices) {
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

	private void addAnnotatedArguments(List<SleuthAnnotatedParameterContainer> toBeAdded) {
		for (SleuthAnnotatedParameterContainer container : toBeAdded) {
			String tagValue = resolveTagValue(container.getAnnotation(), container.getArgument());
			tracer.addTag(container.getAnnotation().value(), tagValue);
		}
	}

	String resolveTagValue(SpanTag annotation, Object argument) {
		if (argument == null) {
			return "null";
		}
		if (StringUtils.isNotBlank(annotation.tagValueResolverBeanName())) {
			SleuthTagValueResolver tagValueResolver = context.getBean(annotation.tagValueResolverBeanName(),
					SleuthTagValueResolver.class);
			if (tagValueResolver != null) {
				return tagValueResolver.resolveTagValue(argument);
			}
		} else if (StringUtils.isNotBlank(annotation.tagValueExpression())) {
			try {
				ExpressionParser expressionParser = new SpelExpressionParser();

				Expression expression = expressionParser.parseExpression(annotation.tagValueExpression());
				return expression.getValue(argument, String.class);
			} catch (Exception e) {
			}
		}
		return argument.toString();
	}

	private List<SleuthAnnotatedParameterContainer> findAnnotatedParameters(Method method, Object[] args) {
		Annotation[][] parameters = method.getParameterAnnotations();
		List<SleuthAnnotatedParameterContainer> result = new ArrayList<>();
		int i = 0;
		for (Annotation[] parameter : parameters) {
			for (Annotation parameter2 : parameter) {
				if (parameter2 instanceof SpanTag) {
					SpanTag annotation = (SpanTag) parameter2;
					SleuthAnnotatedParameterContainer container = new SleuthAnnotatedParameterContainer();
					container.setAnnotation(annotation);
					container.setArgument(args[i]);
					container.setParameter(parameter2);
					container.setParameterIndex(i);
					result.add(container);
				}
			}
			i++;
		}
		return result;
	}

}
