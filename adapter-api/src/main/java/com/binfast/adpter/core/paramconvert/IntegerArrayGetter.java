package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

import java.util.ArrayList;
import java.util.List;

public class IntegerArrayGetter extends ParaGetter<Integer[]> {
	
	public IntegerArrayGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Integer[] get(ParamsHandler c) {
		Integer[] ret = c.getParaValuesToInt(getParameterName());
		if( null == ret) {
			ret =  this.getDefaultValue();
		}
		return ret;
	}

	@Override
	protected Integer[] to(String v) {
		if(StrKit.notBlank(v)){
			String[] ss = v.split(",");
			List<Integer> ls = new ArrayList<Integer>(ss.length);
			for(String s : ss){
				if(StrKit.notBlank(s)){
					ls.add(Integer.parseInt(s.trim()));
				}
			}
			return ls.toArray(new Integer[0]);
		}
		return null;
	}
}
