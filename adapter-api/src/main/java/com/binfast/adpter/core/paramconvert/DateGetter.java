/**
 * Copyright (c) 2011-2023, 玛雅牛 (myaniu AT gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.binfast.adpter.core.paramconvert;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.ParamsHandler;
import com.binfast.adpter.core.converter.Converters;
import com.binfast.adpter.core.kit.StrKit;

import java.text.ParseException;

public class DateGetter extends ParaGetter<java.util.Date> {
	private static Converters.DateConverter converter = new Converters.DateConverter();
	public DateGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public java.util.Date get(ParamsHandler c) {
		String value = c.getPara(this.getParameterName());
		if(StrKit.notBlank(value)){
			return to(value);
		}
		return this.getDefaultValue();
	}

	@Override
	protected java.util.Date to(String v) {
		if(StrKit.isBlank(v)){
			return null;
		}
		try {
			return converter.convert(v);
		} catch (ParseException e) {
			// return null;
			throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + v + "\" to java.util.Date");
		}
	}

}
