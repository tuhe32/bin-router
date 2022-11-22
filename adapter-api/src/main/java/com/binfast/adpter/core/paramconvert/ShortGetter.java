package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class ShortGetter extends ParaGetter<Short> {
	
	public ShortGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Short get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		try {
			if (StrKit.isBlank(value))
				return this.getDefaultValue();
			return to(value.trim());
		}
		catch (Exception e) {
			throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + value + "\" to Short value.");
		}
	}

	@Override
	protected Short to(String v) {
		if(StrKit.notBlank(v)){
			return Short.parseShort(v);
		}
		return null;
	}

}
