package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class LongGetter extends ParaGetter<Long> {
	
	public LongGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Long get(ParamsHandler c) {
		return c.getParaToLong(getParameterName(),getDefaultValue());
	}

	@Override
	protected Long to(String v) {
		if(StrKit.notBlank(v)){
			return Long.parseLong(v);
		}
		return null;
	}
}
