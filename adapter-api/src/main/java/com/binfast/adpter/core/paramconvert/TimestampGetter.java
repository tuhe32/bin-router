package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.converter.Converters;
import com.binfast.adpter.core.kit.StrKit;

import java.text.ParseException;

public class TimestampGetter extends ParaGetter<java.sql.Timestamp> {
	private static Converters.TimestampConverter converter = new Converters.TimestampConverter();
	public TimestampGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public java.sql.Timestamp get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		if(StrKit.notBlank(value)){
			return to(value);
		}
		return this.getDefaultValue();
	}

	@Override
	protected java.sql.Timestamp to(String v) {
		if(StrKit.isBlank(v)){
			return null;
		}
		try {
			return converter.convert(v);
		} catch (ParseException e) {
			// return null;
			throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + v + "\" to java.sql.Timestamp");
		}
	}

}
