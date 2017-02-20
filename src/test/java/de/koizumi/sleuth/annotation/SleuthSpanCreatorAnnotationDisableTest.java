package de.koizumi.sleuth.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SleuthAnnotationConfiguration.class,
		properties = "spring.sleuth.annotation.enabled=false")
public class SleuthSpanCreatorAnnotationDisableTest {

	@Autowired(required = false)
	private SleuthSpanTagAnnotationHandler annotationSpanUtil;
	
	@Test
	public void shouldNotAutowireBecauseConfigIsDisabled() {
		assertThat(annotationSpanUtil).isNull();
	}
}
