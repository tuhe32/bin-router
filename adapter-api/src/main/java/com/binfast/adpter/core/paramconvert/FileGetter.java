package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;

import java.io.File;

public class FileGetter extends ParaGetter<File> {

	public FileGetter(String parameterName,String defaultValue) {
		super(parameterName,null);
	}

	@Override
	public File get(ParamsHandler c) {
		String parameterName = this.getParameterName();
//		UploadFile uf = null;
//		if(parameterName.isEmpty()){
//			uf = c.getFile();
//		}else{
//			uf = c.getFile(parameterName);
//		}
//		if(uf != null){
//			return uf.getFile();
//		}
		return null;
	}

	@Override
	protected File to(String v) {
		return null;
	}

}
