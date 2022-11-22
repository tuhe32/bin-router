package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

import java.math.BigInteger;

public class BigIntegerGetter extends ParaGetter<BigInteger> {

	public BigIntegerGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public BigInteger get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		try {
			if (StrKit.isBlank(value))
				return this.getDefaultValue();
			return to(value.trim());
		} catch (Exception e) {
			throw ExceptionFactory.sysException(String.valueOf(400),
					"Can not parse the parameter \"" + value + "\" to BigInteger value.");
		}
	}

	@Override
	protected BigInteger to(String v) {
		if(StrKit.notBlank(v)){
			return new BigInteger(v);
		}
		return null;
	}

}
