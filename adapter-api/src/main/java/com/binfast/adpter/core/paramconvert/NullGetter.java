package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

public class NullGetter extends ParaGetter<Object> {

	public NullGetter(String parameterName, String defaultValue) {
		super(null,null);
	}

	@Override
	public Object get(ParamsHandler c) {
		return null;
	}

	@Override
	protected Object to(String v) {
		return null;
	}
}
