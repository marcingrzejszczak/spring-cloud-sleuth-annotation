package de.koizumi.sleuth.annotation;
import java.lang.annotation.Annotation;

class SleuthAnnotatedParameterContainer {

	private int parameterIndex;
	private SpanTag annotation;
	private Object argument;
	private Annotation parameter;

	SpanTag getAnnotation() {
		return this.annotation;
	}

	void setAnnotation(SpanTag annotation) {
		this.annotation = annotation;
	}

	Object getArgument() {
		return this.argument;
	}

	void setArgument(Object argument) {
		this.argument = argument;
	}

	Annotation getParameter() {
		return this.parameter;
	}

	void setParameter(Annotation parameter) {
		this.parameter = parameter;
	}

	int getParameterIndex() {
		return this.parameterIndex;
	}

	void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

}
