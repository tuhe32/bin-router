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
package com.binfast.adpter.openapi;

import com.binfast.adpter.openapi.constants.DocGlobalConstants;
import com.binfast.adpter.openapi.constants.Methods;
import com.binfast.adpter.openapi.helper.JavaProjectBuilderHelper;
import com.binfast.adpter.openapi.model.*;
import com.binfast.adpter.openapi.model.openapi.OpenApiTag;
import com.binfast.adpter.openapi.utils.JsonUtil;
import com.binfast.adpter.openapi.utils.OpenApiSchemaUtil;
import com.power.common.util.CollectionUtil;
import com.power.common.util.FileUtil;
import com.power.common.util.StringUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.util.*;

import static com.binfast.adpter.openapi.constants.DocGlobalConstants.ARRAY;
import static com.binfast.adpter.openapi.constants.DocGlobalConstants.OPENAPI_3_COMPONENT_KRY;


/**
 * @author xingzi
 */
public class OpenApiBuilder extends AbstractOpenApiBuilder {

    private static final OpenApiBuilder INSTANCE = new OpenApiBuilder();

    /**
     *  For unit testing
     *
     * @param config Configuration of smart-doc
     */
    public static void buildOpenApi(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = JavaProjectBuilderHelper.create();
        buildOpenApi(config, javaProjectBuilder);
    }

    /**
     * Only for smart-doc maven plugin and gradle plugin.
     *
     * @param config         Configuration of smart-doc
     * @param projectBuilder JavaDocBuilder of QDox
     */
    public static void buildOpenApi(ApiConfig config, JavaProjectBuilder projectBuilder) {
        List<ApiDoc> apiDocList = INSTANCE.getOpenApiDocs(config,projectBuilder);
        INSTANCE.openApiCreate(config, apiDocList);
        System.out.println("API文档已生成完毕");
    }

    /**
     * Build OpenApi
     *
     * @param config     Configuration of smart-doc
     * @param apiDocList List of API DOC
     */
    @Override
    public void openApiCreate(ApiConfig config, List<ApiDoc> apiDocList) {
        Map<String, Object> json = new HashMap<>(8);
        json.put("openapi", "3.0.3");
        json.put("info", buildInfo(config));
        json.put("servers", buildServers(config));
        Set<OpenApiTag> tags = new HashSet<>();
        json.put("tags", tags);
        json.put("paths", buildPaths(config, apiDocList, tags));
        json.put("components", buildComponentsSchema(apiDocList));

        String filePath = config.getOutPath();
        filePath = filePath + DocGlobalConstants.OPEN_API_JSON;
        String data = JsonUtil.toPrettyJson(json);
        FileUtil.nioWriteFile(data, filePath);
    }

    /**
     * Build openapi info
     *
     * @param apiConfig Configuration of smart-doc
     */
    private static Map<String, Object> buildInfo(ApiConfig apiConfig) {
        Map<String, Object> infoMap = new HashMap<>(8);
        infoMap.put("title", apiConfig.getProjectName() == null ? "Project Name is Null." : apiConfig.getProjectName());
        infoMap.put("version", "1.0.0");
        return infoMap;
    }

    /**
     * Build Servers
     *
     * @param config Configuration of smart-doc
     */
    private static List<Map<String, Object>> buildServers(ApiConfig config) {
        List<Map<String, Object>> serverList = new ArrayList<>();
        Map<String, Object> serverMap = new HashMap<>(8);
        serverMap.put("url", config.getServerUrl() == null ? "" : config.getServerUrl());
        serverList.add(serverMap);
        return serverList;
    }


    /**
     * Build request
     *
     * @param apiConfig    Configuration of smart-doc
     * @param apiMethodDoc ApiMethodDoc
     * @param apiDoc       apiDoc
     */
    public Map<String, Object> buildPathUrlsRequest(ApiConfig apiConfig, ApiMethodDoc apiMethodDoc, ApiDoc apiDoc) {
        Map<String, Object> request = new HashMap<>(20);
        request.put("summary", apiMethodDoc.getDesc());
        request.put("description", apiMethodDoc.getDetail());
        if (StringUtil.isNotEmpty(apiMethodDoc.getGroup())) {
            request.put("tags", new String[]{apiDoc.getDesc()});
        } else {
            request.put("tags", new String[]{apiDoc.getDesc()});
        }
        request.put("requestBody", buildRequestBody(apiConfig, apiMethodDoc, OPENAPI_3_COMPONENT_KRY));
        request.put("parameters", buildParameters(apiMethodDoc));
        request.put("responses", buildResponses(apiConfig, apiMethodDoc));
        request.put("deprecated", apiMethodDoc.isDeprecated());
        request.put("operationId", String.join("", OpenApiSchemaUtil.getPatternResult("[A-Za-z0-9{}]*", apiMethodDoc.getPath())));

        return request;
    }

    /**
     * Build request body
     *
     * @param apiMethodDoc ApiMethodDoc
     */
    private static Map<String, Object> buildRequestBody(ApiConfig apiConfig, ApiMethodDoc apiMethodDoc, String componentKey) {
        Map<String, Object> requestBody = new HashMap<>(8);
        boolean isPost = (apiMethodDoc.getType().equals(Methods.POST.getValue())
            || apiMethodDoc.getType().equals(Methods.PUT.getValue()) ||
            apiMethodDoc.getType().equals(Methods.PATCH.getValue()));
        //add content of post method
        if (isPost) {
            requestBody.put("content", buildContent(apiConfig, apiMethodDoc, false, componentKey));
            return requestBody;
        }
        return null;
    }


    /**
     * response body
     *
     * @param apiMethodDoc ApiMethodDoc
     * @return response body
     */
    @Override
    public Map<String, Object> buildResponsesBody(ApiConfig apiConfig, ApiMethodDoc apiMethodDoc) {
        Map<String, Object> responseBody = new HashMap<>(10);
        responseBody.put("description", "OK");
        responseBody.put("content", buildContent(apiConfig, apiMethodDoc, true, OPENAPI_3_COMPONENT_KRY));
        return responseBody;
    }

    @Override
    List<Map<String, Object>> buildParameters(ApiMethodDoc apiMethodDoc) {
        Map<String, Object> parameters;
        List<Map<String, Object>> parametersList = new ArrayList<>();
        // Handling path parameters
        for (ApiParam apiParam : apiMethodDoc.getPathParams()) {
            parameters = getStringParams(apiParam, apiParam.isHasItems());
            parameters.put("in", "path");
            List<ApiParam> children = apiParam.getChildren();
            if (CollectionUtil.isEmpty(children)) {
                parametersList.add(parameters);
            }
        }
        for (ApiParam apiParam : apiMethodDoc.getQueryParams()) {
            if (apiParam.isHasItems()) {
                parameters = getStringParams(apiParam, false);
                Map<String, Object> arrayMap = new HashMap<>();
                arrayMap.put("type", ARRAY);
                arrayMap.put("items", getStringParams(apiParam, apiParam.isHasItems()));
                parameters.put("schema", arrayMap);
                parametersList.add(parameters);
            } else {
                parameters = getStringParams(apiParam, false);
                List<ApiParam> children = apiParam.getChildren();
                if (CollectionUtil.isEmpty(children)) {
                    parametersList.add(parameters);
                }
            }
        }
        //with headers
        if (!CollectionUtil.isEmpty(apiMethodDoc.getRequestHeaders())) {
            for (ApiReqParam header : apiMethodDoc.getRequestHeaders()) {
                parameters = new HashMap<>(20);
                parameters.put("name", header.getName());
                parameters.put("description", header.getDesc());
                parameters.put("required", header.isRequired());
                parameters.put("example", header.getValue());
                parameters.put("schema", buildParametersSchema(header));
                parameters.put("in", "header");
                parametersList.add(parameters);
            }
        }
        return parametersList;
    }

    @Override
    Map<String, Object> getStringParams(ApiParam apiParam, boolean hasItems) {
        Map<String, Object> parameters;
        parameters = new HashMap<>(20);
        boolean isFile = "file".equalsIgnoreCase(apiParam.getType());
        if (!hasItems) {
            parameters.put("name", apiParam.getField());
            parameters.put("description", apiParam.getDesc());
            parameters.put("required", apiParam.isRequired());
            parameters.put("example", StringUtil.removeQuotes(apiParam.getValue()));
        } else {
            if (isFile) {
                parameters.put("type", "string");
                parameters.put("format", "binary");
            } else {
                parameters.put("type", apiParam.getType());
            }
        }
        if (isFile) {
            parameters.put("in", "formData");
        } else {
            parameters.put("in", "query");
        }
        parameters.put("schema", buildParametersSchema(apiParam));
        return parameters;
    }

    @Override
    public Map<String, Object> buildComponentsSchema(List<ApiDoc> apiDocs) {
            Map<String, Object> schemas = new HashMap<>(4);
            Map<String, Object> component = new HashMap<>();
            component.put("string", STRING_COMPONENT);
            apiDocs.forEach(
                    a -> {
                        List<ApiMethodDoc> apiMethodDocs = a.getList();
                        apiMethodDocs.forEach(
                                method -> {
                                    //request components
                                    String requestSchema = OpenApiSchemaUtil.getClassNameFromParams(method.getRequestParams());
                                    List<ApiParam> requestParams = method.getRequestParams();
                                    Map<String, Object> prop = buildProperties(requestParams, component,OPENAPI_3_COMPONENT_KRY);
                                    component.put(requestSchema, prop);
                                    //response components
                                    List<ApiParam> responseParams = method.getResponseParams();
                                    String schemaName = OpenApiSchemaUtil.getClassNameFromParams(method.getResponseParams());
                                    component.put(schemaName, buildProperties(responseParams, component,OPENAPI_3_COMPONENT_KRY));
                                }
                        );
                    }
            );
            component.remove(OpenApiSchemaUtil.NO_BODY_PARAM);
            schemas.put("schemas", component);
            return schemas;
        }


}
