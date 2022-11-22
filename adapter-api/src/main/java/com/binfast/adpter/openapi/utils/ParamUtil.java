package com.binfast.adpter.openapi.utils;

import com.power.common.util.StringUtil;
import com.binfast.adpter.openapi.ProjectDocConfigBuilder;
import com.binfast.adpter.openapi.constants.DocGlobalConstants;
import com.binfast.adpter.openapi.constants.DocTags;
import com.binfast.adpter.openapi.model.ApiParam;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:cqmike0315@gmail.com">chenqi</a>
 * @version 1.0
 */
public class ParamUtil {

    public static JavaClass handleSeeEnum(ApiParam param, JavaField javaField, ProjectDocConfigBuilder builder, boolean jsonRequest,
        Map<String, String> tagsMap) {
        JavaClass seeEnum = JavaClassUtil.getSeeEnum(javaField, builder);
        if (Objects.isNull(seeEnum)) {
            return null;
        }
        param.setType(DocGlobalConstants.ENUM);
        Object value = JavaClassUtil.getEnumValue(seeEnum, !jsonRequest);
        param.setValue(String.valueOf(value));
        param.setEnumValues(JavaClassUtil.getEnumValues(seeEnum));
        param.setEnumInfo(JavaClassUtil.getEnumInfo(seeEnum, builder));
        // Override old value
        if (tagsMap.containsKey(DocTags.MOCK) && StringUtil.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
            param.setValue(tagsMap.get(DocTags.MOCK));
        }
        return seeEnum;
    }
}
