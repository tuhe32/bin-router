package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

public class StringGetter extends ParaGetter<String> {

	public StringGetter(String parameterName, String defaultValue){
		super(parameterName, defaultValue);
	}

	@Override
	public String get(ParamsHandler c) {
		return c.getPara(getParameterName(), getDefaultValue());
	}

	@Override
	protected String to(String v) {
		return v;
	}
}
