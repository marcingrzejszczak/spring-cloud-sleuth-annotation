package de.koizumi.sleuth.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.sleuth.annotation.enabled", matchIfMissing = true)
public class SleuthAnnotationConfiguration {
	
	@Autowired
	private Tracer tracer;
	
	@Bean
	public SleuthSpanTagAnnotationHandler spanUtil(ApplicationContext context) {
		return new SleuthSpanTagAnnotationHandler(context, this.tracer);
	}

	@ConditionalOnMissingBean(SleuthSpanCreator.class)
	@Bean
	SleuthSpanCreator spanCreator() {
		return new DefaultSleuthSpanCreator(this.tracer, spanUtil(null));
	}
	
	@Bean
	public SleuthSpanCreateBeanPostProcessor sleuthSpanCreateBeanPostProcessor(SleuthSpanCreator spanCreator) {
		return new SleuthSpanCreateBeanPostProcessor(new SleuthSpanCreatorAdvice(spanCreator, tracer));
	}
	
}
