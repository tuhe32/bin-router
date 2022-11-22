/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2022 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.binfast.adpter.openapi.handler;

import com.power.common.util.StringUtil;
import com.power.common.util.UrlUtil;
import com.binfast.adpter.openapi.ProjectDocConfigBuilder;
import com.binfast.adpter.openapi.constants.DocGlobalConstants;
import com.binfast.adpter.openapi.function.RequestMappingFunc;
import com.binfast.adpter.openapi.model.annotation.FrameworkAnnotations;
import com.binfast.adpter.openapi.model.request.RequestMapping;
import com.binfast.adpter.openapi.utils.DocUrlUtil;
import com.binfast.adpter.openapi.utils.DocUtil;
import com.thoughtworks.qdox.model.JavaMethod;

import java.util.Objects;

/**
 * @author yu3.sun on 2022/10/1
 */
public interface IRequestMappingHandler {

    default RequestMapping formatMappingData(ProjectDocConfigBuilder projectBuilder, String controllerBaseUrl, RequestMapping requestMapping) {
        String shortUrl = requestMapping.getShortUrl();
        if (Objects.nonNull(shortUrl)) {
            String serverUrl = projectBuilder.getServerUrl();
            String contextPath = projectBuilder.getApiConfig().getPathPrefix();
            shortUrl = StringUtil.removeQuotes(shortUrl);
            String url = DocUrlUtil.getMvcUrls(serverUrl, contextPath + "/" + controllerBaseUrl, shortUrl);
            shortUrl = DocUrlUtil.getMvcUrls(DocGlobalConstants.EMPTY, contextPath + "/" + controllerBaseUrl, shortUrl);
            String urlSuffix = projectBuilder.getApiConfig().getUrlSuffix();
            if (StringUtil.isEmpty(urlSuffix)) {
                urlSuffix = StringUtil.EMPTY;
            }
            url = UrlUtil.simplifyUrl(StringUtil.trim(url)) + urlSuffix;
            shortUrl = UrlUtil.simplifyUrl(StringUtil.trim(shortUrl)) + urlSuffix;
            url = DocUtil.formatPathUrl(url);
            requestMapping.setUrl(url).setShortUrl(shortUrl);
            return requestMapping;
        }
        return requestMapping;
    }

    RequestMapping handle(ProjectDocConfigBuilder projectBuilder, String controllerBaseUrl, JavaMethod method,
        FrameworkAnnotations frameworkAnnotations,
        RequestMappingFunc requestMappingFunc);
}
