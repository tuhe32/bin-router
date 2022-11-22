package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.converter.Converters;
import com.binfast.adpter.core.kit.StrKit;

import java.text.ParseException;

public class SqlDateGetter extends ParaGetter<java.sql.Date> {
	private static Converters.SqlDateConverter converter = new Converters.SqlDateConverter();
	public SqlDateGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public java.sql.Date get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		if(StrKit.notBlank(value)){
			return to(value);
		}
		return this.getDefaultValue();
	}

	@Override
	protected java.sql.Date to(String v) {
		if(StrKit.isBlank(v)){
			return null;
		}
		try {
			return converter.convert(v);
		} catch (ParseException e) {
			// return null;
			throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + v + "\" to java.sql.Date");
		}
	}

}
