package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

/**
 * 无参 action 共享同一个 NullParaProcessor 对象，节省空间
 * 
 * 其它所有 ParaProcessor 对象的 get(Action action, Controller c)
 * 内部不必进行 null 值判断，节省时间
 */
public class NullParaProcessor extends ParaProcessor {
	
	private static final Object[] NULL_ARGS = new Object[0];
	
	public static final NullParaProcessor me = new NullParaProcessor(0);
	
	private NullParaProcessor(int paraCount) {
		super(paraCount);
	}
	
	@Override
	public Object[] get(ParamsHandler c) {
		return NULL_ARGS;
	}
}






