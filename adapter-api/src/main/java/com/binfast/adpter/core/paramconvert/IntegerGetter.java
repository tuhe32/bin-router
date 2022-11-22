package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class IntegerGetter extends ParaGetter<Integer> {

	public IntegerGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public Integer get(ParamsHandler c) {
		return c.getParaToInt(getParameterName(),getDefaultValue());
	}

	@Override
	protected Integer to(String v) {
		if(StrKit.notBlank(v)){
			return Integer.parseInt(v);
		}
		return null;
	}
}
