package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

public class StringArrayGetter extends ParaGetter<String[]> {
	
	public StringArrayGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}
	@Override
	public String[] get(ParamsHandler c) {
		String[] ret = c.getParaValues(getParameterName());
		if( null == ret) {
			ret =  this.getDefaultValue();
		}
		return ret;
	}
	@Override
	protected String[] to(String v) {
		if(StrKit.notBlank(v)){
			return v.split(",");
		}
		return null;
	}
}
