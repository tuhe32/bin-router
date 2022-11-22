package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

public class RawDataGetter extends ParaGetter<RawData>{

	public RawDataGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public RawData get(ParamsHandler c) {
		return new RawData(c.getRawData());
	}

	@Override
	protected RawData to(String v) {
		return new RawData(v);
	}
}
