package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

/**
 * 使用构建好的 IParaGetter 数组获取用于 action 方法实参的参数值
 */
public class ParaProcessor implements IParaGetter<Object[]> {
	
	private int fileParaIndex = -1;
	private IParaGetter<?>[] paraGetters;
	
	public ParaProcessor(int paraCount) {
		paraGetters = paraCount > 0 ? new IParaGetter<?>[paraCount] : null;
	}
	
	public void addParaGetter(int index, IParaGetter<?> paraGetter) {
		// fileParaIndex 记录第一个 File、UploadFile 的数组下标
		if (	fileParaIndex == -1 &&
				(paraGetter instanceof FileGetter || paraGetter instanceof UploadFileGetter)) {
			fileParaIndex = index;
		}
		
		paraGetters[index] = paraGetter;
	}
	
	@Override
	public Object[] get(ParamsHandler c) {
		int len = paraGetters.length;
		Object[] ret = new Object[len];
		
		// 没有 File、UploadFile 参数的 action
		if (fileParaIndex == -1) {
			for (int i=0; i<len; i++) {
				ret[i] = paraGetters[i].get(c);
			}
			return ret;
		}
		
		// 有 File、UploadFile 参数的 action，优先获取 File、UploadFile 对象
//		Object fileRet = paraGetters[fileParaIndex].get(action, c);
//		for (int i=0; i<len; i++) {
//			if (i != fileParaIndex) {
//				ret[i] = paraGetters[i].get(action, c);
//			} else {
//				ret[i] = fileRet;
//			}
//		}
		return ret;
	}
}






