package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class DoubleGetter extends ParaGetter<Double> {
	
	public DoubleGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Double get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		try {
			if (StrKit.isBlank(value))
				return this.getDefaultValue();
			value = value.trim();
			return to(value);
		} catch (Exception e) {
			throw ExceptionFactory.sysException(String.valueOf(400),
					"Can not parse the parameter \"" + value + "\" to Double value.");
		}
	}

	@Override
	protected Double to(String v) {
		if(StrKit.notBlank(v)){
			return Double.parseDouble(v);
		}
		return null;
	}
}
