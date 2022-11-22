package com.binfast.adpter.core.paramconvert;

public abstract class ParaGetter<T> implements IParaGetter<T> {
	private final String parameterName;
	private final T defaultValue;
	
	protected final String getParameterName() {
		return parameterName;
	}
	
	protected final T getDefaultValue() {
		return defaultValue;
	}
	
	public ParaGetter(String parameterName, String defaultValue){
		this.parameterName = parameterName;
		this.defaultValue = to(defaultValue);
	}
	
	protected abstract T to(String v);
}
