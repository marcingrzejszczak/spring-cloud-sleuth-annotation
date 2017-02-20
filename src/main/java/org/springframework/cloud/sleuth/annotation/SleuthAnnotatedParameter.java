package org.springframework.cloud.sleuth.annotation;
class SleuthAnnotatedParameter {

	private int parameterIndex;
	private SpanTag annotation;
	private Object argument;

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

	int getParameterIndex() {
		return this.parameterIndex;
	}

	void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

}
