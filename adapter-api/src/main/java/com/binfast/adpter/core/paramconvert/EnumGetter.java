package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.kit.StrKit;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumGetter<T extends Enum> extends ParaGetter<T> {
	private final Class<T> enumType;
	public EnumGetter(Class<T> enumType, String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
		this.enumType = enumType;
	}

	@Override
	public T get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		if(StrKit.notBlank(value)){
			return to(value);
		}
		return this.getDefaultValue();
	}

	@Override
	protected T to(String v) {
		if(StrKit.notBlank(v)){
			try{
				return (T) Enum.valueOf(this.enumType, v.trim());
			}catch(Exception e){
				return null;
			}
		}
		return null;
	}
}
