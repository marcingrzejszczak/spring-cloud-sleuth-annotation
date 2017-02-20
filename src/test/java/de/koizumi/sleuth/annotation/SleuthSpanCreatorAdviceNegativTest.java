package de.koizumi.sleuth.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.log.NoOpSpanLogger;
import org.springframework.cloud.sleuth.log.SpanLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.koizumi.sleuth.annotation.SleuthSpanCreatorAdviceNegativTest.TestConfiguration;

@SpringBootTest(classes = TestConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SleuthSpanCreatorAdviceNegativTest {

	@Autowired
	private NotAnnotatedTestBeanI testBean;
	
	@Autowired
	private TestBeanI annotatedTestBean;
	
	@Autowired
	private SleuthSpanCreatorAdviceHolder adviceHolder;
	
	@Autowired
	private Tracer tracer;
	
	@Before
	public void setup() {
		Mockito.reset(tracer);
		Mockito.when(tracer.isTracing()).thenReturn(true);
	}

	@Test
	public void shouldNotCallAdviceForNotAnnotatedBean() {
		testBean.testMethod();
		
		Mockito.verifyZeroInteractions(adviceHolder.advice);
	}

	@Test
	public void shouldCallAdviceForAnnotatedBean() throws Throwable {
		annotatedTestBean.testMethod();
		
		Mockito.verify(tracer).createSpan(Mockito.eq("TestBeanI/testMethod"), Mockito.<Span> any());
		Mockito.verify(adviceHolder.advice).instrumentOnMethodAnnotation(Mockito.<ProceedingJoinPoint> any());
		
	}
	
	protected interface NotAnnotatedTestBeanI {

		void testMethod();
	}

	protected static class NotAnnotatedTestBean implements NotAnnotatedTestBeanI {

		@Override
		public void testMethod() {
		}

	}
	
	protected interface TestBeanI {
		
		@NewSpan
		void testMethod();
		
		void testMethod2();
		
		void testMethod3();
		
		@NewSpan(name = "testMethod4")
		void testMethod4();
		
		@NewSpan(name = "testMethod5")
		void testMethod5(@SpanTag("testTag") String test);
		
		void testMethod6(String test);
		
		void testMethod7();
	}
	
	protected static class TestBean implements TestBeanI {

		@Override
		public void testMethod() {
		}

		@NewSpan
		@Override
		public void testMethod2() {
		}

		@NewSpan(name = "testMethod3")
		@Override
		public void testMethod3() {
		}

		@Override
		public void testMethod4() {
		}
		
		@Override
		public void testMethod5(String test) {
		}

		@NewSpan(name = "testMethod6")
		@Override
		public void testMethod6(@SpanTag("testTag6") String test) {
			
		}

		@Override
		public void testMethod7() {
		}
	}

	@Configuration
	@Import({ TraceAutoConfiguration.class, CreateSleuthTestConfiguration.class })
	protected static class TestConfiguration {

	}

	@Configuration
	protected static class CreateSleuthTestConfiguration {
		
		@Bean
		public SleuthSpanCreatorAdviceHolder adviceHolder(SleuthSpanCreator spanCreator) {
			SleuthSpanCreatorAdvice advice = new SleuthSpanCreatorAdvice(spanCreator, tracer());
			advice = Mockito.spy(advice);
			SleuthSpanCreatorAdviceHolder adviceHolder = new SleuthSpanCreatorAdviceHolder();
			adviceHolder.setAdvice(advice);
			return adviceHolder;
		}
		
		@Bean
		public SleuthSpanTagAnnotationHandler spanUtil(ApplicationContext context) {
			return new SleuthSpanTagAnnotationHandler(context, tracer());
		}

		@Bean
		public SleuthSpanCreateBeanPostProcessor sleuthSpanCreateBeanPostProcessor(SleuthSpanCreatorAdviceHolder adviceHolder) {
			SleuthSpanCreateBeanPostProcessor postProcessor = new SleuthSpanCreateBeanPostProcessor(adviceHolder.advice);
			return postProcessor;
		}

		@Bean
		public NotAnnotatedTestBeanI testBean() {
			return new NotAnnotatedTestBean();
		}

		@Bean
		public SleuthSpanCreator sleuthSpanCreator(SleuthSpanTagAnnotationHandler annotationSpanUtil) {
			return new DefaultSleuthSpanCreator(tracer(), annotationSpanUtil);
		}
		
		@Bean
		public TestBeanI annotatedTestBean() {
			return new TestBean();
		}
		
		@Bean
		public Tracer tracer() {
			return Mockito.mock(Tracer.class);
		}

		@Bean
		public SpanLogger spanLogger() {
			return new NoOpSpanLogger();
		}
	}
	
	protected static class SleuthSpanCreatorAdviceHolder {
		
		private SleuthSpanCreatorAdvice advice;

		public SleuthSpanCreatorAdvice getAdvice() {
			return advice;
		}

		public void setAdvice(SleuthSpanCreatorAdvice advice) {
			this.advice = advice;
		}
		
		
	}
}
