package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class FloatGetter extends ParaGetter<Float> {

	public FloatGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public Float get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		try {
			if (StrKit.isBlank(value))
				return this.getDefaultValue();
			return to(value.trim());
		} catch (Exception e) {
			throw ExceptionFactory.sysException(String.valueOf(400),
					"Can not parse the parameter \"" + value + "\" to Float value.");
		}
	}

	@Override
	protected Float to(String v) {
		if(StrKit.notBlank(v)){
			return Float.parseFloat(v);
		}
		return null;
	}
}
