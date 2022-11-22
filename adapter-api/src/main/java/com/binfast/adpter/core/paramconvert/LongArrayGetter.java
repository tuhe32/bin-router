package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

import java.util.ArrayList;
import java.util.List;

public class LongArrayGetter extends ParaGetter<Long[]> {

	public LongArrayGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Long[] get(ParamsHandler c) {
		Long[] ret =  c.getParaValuesToLong(getParameterName());
		if( null == ret) {
			ret =  this.getDefaultValue();
		}
		return ret;
	}

	@Override
	protected Long[] to(String v) {
		if(StrKit.notBlank(v)){
			String[] ss = v.split(",");
			List<Long> ls = new ArrayList<Long>(ss.length);
			for(String s : ss){
				if(StrKit.notBlank(s)){
					ls.add(Long.parseLong(s.trim()));
				}
			}
			return ls.toArray(new Long[0]);
		}
		return null;
	}
}
