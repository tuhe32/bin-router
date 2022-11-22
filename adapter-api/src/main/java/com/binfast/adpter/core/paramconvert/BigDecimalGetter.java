package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

import java.math.BigDecimal;

public class BigDecimalGetter extends ParaGetter<BigDecimal> {

	public BigDecimalGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public BigDecimal get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		try {
			if (StrKit.isBlank(value))
				return this.getDefaultValue();
			return to(value.trim());
		} catch (Exception e) {
			throw ExceptionFactory.sysException(String.valueOf(400),
					"Can not parse the parameter \"" + value + "\" to BigDecimal value.");
		}
	}

	@Override
	protected BigDecimal to(String v) {
		if(StrKit.notBlank(v)){
			return new BigDecimal(v);
		}
		return null;
	}

}
