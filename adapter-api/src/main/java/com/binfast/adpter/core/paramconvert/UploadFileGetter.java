package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import org.springframework.web.multipart.MultipartFile;

public class UploadFileGetter extends ParaGetter<MultipartFile> {

	public UploadFileGetter(String parameterName,String defaultValue) {
		super(parameterName,null);
	}

	@Override
	public MultipartFile get(ParamsHandler c) {
		String parameterName = this.getParameterName();
		if(parameterName.isEmpty()){
			return c.getFile();
		}
		return c.getFile(parameterName);
	}

	@Override
	protected MultipartFile to(String v) {
		return null;
	}

}
