package de.koizumi.sleuth.annotation;
import java.lang.annotation.Annotation;

public class SleuthAnnotatedParameterContainer {

	private int parameterIndex;
	private SpanTag annotation;
	private Object argument;
	private Annotation parameter;

	public SpanTag getAnnotation() {
		return this.annotation;
	}

	public void setAnnotation(SpanTag annotation) {
		this.annotation = annotation;
	}

	public Object getArgument() {
		return this.argument;
	}

	public void setArgument(Object argument) {
		this.argument = argument;
	}

	public Annotation getParameter() {
		return this.parameter;
	}

	public void setParameter(Annotation parameter) {
		this.parameter = parameter;
	}

	public int getParameterIndex() {
		return this.parameterIndex;
	}

	public void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

}
