package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class BooleanGetter extends ParaGetter<Boolean> {
	
	public BooleanGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Boolean get(ParamsHandler c) {
		return c.getParaToBoolean(getParameterName(),getDefaultValue());
	}

	@Override
	protected Boolean to(String v) {
		if(StrKit.notBlank(v)){
			return Boolean.parseBoolean(v);
		}
		return null;
	}

}
