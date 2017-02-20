package org.springframework.cloud.sleuth.annotation;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This bean post processor analyzes whether the bean is eligible to
 * be wrapped with an aspect. That will be the case only if there are
 * {@link NewSpan} annotations either on the class or the interface
 * methods.
 *
 * @author Christian Schwerdtfeger
 * @since 1.2.0
 */
class SleuthSpanCreateBeanPostProcessor implements BeanPostProcessor {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final SleuthSpanCreatorAdvice advice;

	SleuthSpanCreateBeanPostProcessor(SleuthSpanCreatorAdvice advice) {
		this.advice = advice;
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		boolean atLeastOneMethodAnnotated = false;
		for (Method method : bean.getClass().getMethods()) {
			if (SleuthAnnotationUtils.isMethodAnnotated(method)) {
				atLeastOneMethodAnnotated = true;
				if (log.isDebugEnabled()) {
					log.debug("Found a method with @NewSpan annotation");
				}
				break;
			}
		}
		if (!atLeastOneMethodAnnotated && (AopUtils.isAopProxy(bean) || AopUtils.isCglibProxy(bean) || AopUtils.isJdkDynamicProxy(bean))) {
			Class<?> beanTargetClass = AopUtils.getTargetClass(bean);
			for (Method method : beanTargetClass.getMethods()) {
				if (SleuthAnnotationUtils.isMethodAnnotated(method)) {
					atLeastOneMethodAnnotated = true;
					if (log.isDebugEnabled()) {
						log.debug("Found a method with @NewSpan annotation on a proxy bean");
					}
					break;
				}
			}
		}
		if (!atLeastOneMethodAnnotated) {
			if (log.isTraceEnabled()) {
				log.trace("Not a single method was annotated with @NewSpan");
			}
			return bean;
		}
		if (log.isDebugEnabled()) {
			log.debug("The object is eligible to be advised");
		}
		AspectJProxyFactory factory = new AspectJProxyFactory(bean);
		factory.addAspect(this.advice);
		return factory.getProxy();
	}

}
