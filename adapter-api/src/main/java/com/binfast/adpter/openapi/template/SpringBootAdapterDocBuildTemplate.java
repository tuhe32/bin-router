package com.binfast.adpter.openapi.template;

import com.binfast.adpter.openapi.ProjectDocConfigBuilder;
import com.binfast.adpter.openapi.constants.*;
import com.binfast.adpter.openapi.handler.SpringMVCRequestHeaderHandler;
import com.binfast.adpter.openapi.handler.SpringMVCRequestMappingHandler;
import com.binfast.adpter.openapi.model.ApiConfig;
import com.binfast.adpter.openapi.model.ApiDoc;
import com.binfast.adpter.openapi.model.ApiReqParam;
import com.binfast.adpter.openapi.model.annotation.*;
import com.binfast.adpter.openapi.model.request.RequestMapping;
import com.binfast.adpter.openapi.utils.JavaClassValidateUtil;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 刘斌
 * @date 2022/11/17 5:17 下午
 */
public class SpringBootAdapterDocBuildTemplate implements IDocBuildTemplate<ApiDoc>, IRestDocTemplate{

    @Override
    public List<ApiDoc> getApiData(ProjectDocConfigBuilder projectBuilder) {
        ApiConfig apiConfig = projectBuilder.getApiConfig();
        List<ApiReqParam> configApiReqParams = Stream.of(apiConfig.getRequestHeaders(), apiConfig.getRequestParams()).filter(Objects::nonNull)
                .flatMap(Collection::stream).collect(Collectors.toList());
        FrameworkAnnotations frameworkAnnotations = registeredAnnotations();
        List<ApiDoc> apiDocList = this.processApiData(projectBuilder, frameworkAnnotations,
                configApiReqParams, new SpringMVCRequestMappingHandler(), new SpringMVCRequestHeaderHandler());
        // sort
        if (apiConfig.isSortByTitle()) {
            Collections.sort(apiDocList);
        }
        return apiDocList;
    }

    @Override
    public boolean ignoreReturnObject(String typeName, List<String> ignoreParams) {
        return JavaClassValidateUtil.isMvcIgnoreParams(typeName, ignoreParams);
    }

    @Override
    public FrameworkAnnotations registeredAnnotations() {
        FrameworkAnnotations annotations = FrameworkAnnotations.builder();
        HeaderAnnotation headerAnnotation = HeaderAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.REQUEST_HERDER)
                .setValueProp(DocAnnotationConstants.VALUE_PROP)
                .setDefaultValueProp(DocAnnotationConstants.DEFAULT_VALUE_PROP)
                .setRequiredProp(DocAnnotationConstants.REQUIRED_PROP);
        // add header annotation
        annotations.setHeaderAnnotation(headerAnnotation);

        // add entry annotation
        Map<String, EntryAnnotation> entryAnnotations = new HashMap<>();
        EntryAnnotation controllerAnnotation = EntryAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.CONTROLLER)
                .setAnnotationFullyName(SpringMvcAnnotations.CONTROLLER);
        entryAnnotations.put(controllerAnnotation.getAnnotationName(), controllerAnnotation);

        EntryAnnotation restController = EntryAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.REST_CONTROLLER);
        entryAnnotations.put(restController.getAnnotationName(), restController);
        annotations.setEntryAnnotations(entryAnnotations);

        // add request body annotation
        RequestBodyAnnotation bodyAnnotation = RequestBodyAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.REQUEST_BODY)
                .setAnnotationFullyName(SpringMvcAnnotations.REQUEST_BODY_FULLY);
        annotations.setRequestBodyAnnotation(bodyAnnotation);

        // request param annotation
        RequestParamAnnotation requestAnnotation = RequestParamAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.REQUEST_PARAM)
                .setDefaultValueProp(DocAnnotationConstants.DEFAULT_VALUE_PROP)
                .setRequiredProp(DocAnnotationConstants.REQUIRED_PROP);
        annotations.setRequestParamAnnotation(requestAnnotation);

        // add path variable annotation
        PathVariableAnnotation pathVariableAnnotation = PathVariableAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.PATH_VARIABLE)
                .setDefaultValueProp(DocAnnotationConstants.DEFAULT_VALUE_PROP)
                .setRequiredProp(DocAnnotationConstants.REQUIRED_PROP);
        annotations.setPathVariableAnnotation(pathVariableAnnotation);

        // add mapping annotations
        Map<String, MappingAnnotation> mappingAnnotations = new HashMap<>();

        MappingAnnotation requestMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.REQUEST_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setScope("class", "method")
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(requestMappingAnnotation.getAnnotationName(), requestMappingAnnotation);

        MappingAnnotation postMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.POST_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.POST.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(postMappingAnnotation.getAnnotationName(), postMappingAnnotation);

        MappingAnnotation getMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.GET_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.GET.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(getMappingAnnotation.getAnnotationName(), getMappingAnnotation);

        MappingAnnotation putMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.PUT_MAPPING)
                .setProducesProp("produces")
                .setParamsProp("params")
                .setMethodProp("method")
                .setMethodType(Methods.PUT.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(putMappingAnnotation.getAnnotationName(), putMappingAnnotation);

        MappingAnnotation patchMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.PATCH_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.PATCH.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(patchMappingAnnotation.getAnnotationName(), patchMappingAnnotation);

        MappingAnnotation deleteMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(SpringMvcAnnotations.DELETE_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.DELETE.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(deleteMappingAnnotation.getAnnotationName(), deleteMappingAnnotation);

        // Adapter api mapping annotation
        addAdapterMappingAnnotation(mappingAnnotations);

        MappingAnnotation feignClientAnnotation = MappingAnnotation.builder()
                .setAnnotationName(DocGlobalConstants.FEIGN_CLIENT)
                .setAnnotationFullyName(DocGlobalConstants.FEIGN_CLIENT_FULLY);
        mappingAnnotations.put(feignClientAnnotation.getAnnotationName(), feignClientAnnotation);

        annotations.setMappingAnnotations(mappingAnnotations);
        return annotations;
    }

    @Override
    public boolean isEntryPoint(JavaClass javaClass, FrameworkAnnotations frameworkAnnotations) {
        List<JavaAnnotation> classAnnotations = javaClass.getAnnotations();
        for (JavaAnnotation annotation : classAnnotations) {
            String name = annotation.getType().getCanonicalName();
            if (AdapterAnnotations.API_MAPPING.equals(name)
                    || AdapterAnnotations.SERVICE.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> listMvcRequestAnnotations() {
        return SpringMvcRequestAnnotationsEnum.listSpringMvcRequestAnnotations();
    }

    @Override
    public void requestMappingPostProcess(JavaClass javaClass, JavaMethod method, RequestMapping requestMapping) {

    }

    @Override
    public boolean ignoreMvcParamWithAnnotation(String annotation) {
        return JavaClassValidateUtil.ignoreSpringMvcParamWithAnnotation(annotation);
    }

    private void addAdapterMappingAnnotation(Map<String, MappingAnnotation> mappingAnnotations) {
        MappingAnnotation requestMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.API_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setScope("class", "method")
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(requestMappingAnnotation.getAnnotationName(), requestMappingAnnotation);

        MappingAnnotation postMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.POST_API_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.POST.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(postMappingAnnotation.getAnnotationName(), postMappingAnnotation);

        MappingAnnotation getMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.GET_API_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.GET.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(getMappingAnnotation.getAnnotationName(), getMappingAnnotation);

        MappingAnnotation putMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.PUT_API_MAPPING)
                .setProducesProp("produces")
                .setParamsProp("params")
                .setMethodProp("method")
                .setMethodType(Methods.PUT.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(putMappingAnnotation.getAnnotationName(), putMappingAnnotation);

        MappingAnnotation patchMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.PATCH_API_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.PATCH.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(patchMappingAnnotation.getAnnotationName(), patchMappingAnnotation);

        MappingAnnotation deleteMappingAnnotation = MappingAnnotation.builder()
                .setAnnotationName(AdapterAnnotations.DELETE_API_MAPPING)
                .setProducesProp("produces")
                .setMethodProp("method")
                .setParamsProp("params")
                .setMethodType(Methods.DELETE.getValue())
                .setPathProps(DocAnnotationConstants.VALUE_PROP, DocAnnotationConstants.NAME_PROP, DocAnnotationConstants.PATH_PROP);
        mappingAnnotations.put(deleteMappingAnnotation.getAnnotationName(), deleteMappingAnnotation);
    }

}
