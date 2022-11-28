package com.binfast.autoconfigure;

import com.binfast.adpter.core.ApiServlet;
import com.binfast.adpter.core.MyRequestMappingHandlerMapping;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleServletHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author 刘斌
 * @date 2022/11/4 5:08 下午
 */
@Configuration
@EnableConfigurationProperties(AdapterApiProperties.class)
public class AdapterApiConfiguration {

//    @Override
//    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
//        RequestMappingHandlerMapping handlerMapping = new MyRequestMappingHandlerMapping();
//        handlerMapping.setOrder(0);
//        return handlerMapping;
//    }
    @Bean
    public MyRequestMappingHandlerMapping myRequestMappingHandlerMapping(AdapterApiProperties adapterApiProperties) {
        MyRequestMappingHandlerMapping mapping =  new MyRequestMappingHandlerMapping(adapterApiProperties.getApiPrefix());
        mapping.setOrder(6);
//        mapping.setInterceptors(getInterceptors(conversionService, resourceUrlProvider));
//        mapping.setContentNegotiationManager(contentNegotiationManager);
//        mapping.setCorsConfigurations(getCorsConfigurations());

//        PathMatchConfigurer pathConfig = getPathMatchConfigurer();
//        if (pathConfig.getPatternParser() != null) {
//            mapping.setPatternParser(pathConfig.getPatternParser());
//        }
//        else {
//            mapping.setUrlPathHelper(pathConfig.getUrlPathHelperOrDefault());
//            mapping.setPathMatcher(pathConfig.getPathMatcherOrDefault());
//
//            Boolean useSuffixPatternMatch = pathConfig.isUseSuffixPatternMatch();
//            if (useSuffixPatternMatch != null) {
//                mapping.setUseSuffixPatternMatch(useSuffixPatternMatch);
//            }
//            Boolean useRegisteredSuffixPatternMatch = pathConfig.isUseRegisteredSuffixPatternMatch();
//            if (useRegisteredSuffixPatternMatch != null) {
//                mapping.setUseRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch);
//            }
//        }
//        Boolean useTrailingSlashMatch = pathConfig.isUseTrailingSlashMatch();
//        if (useTrailingSlashMatch != null) {
//            mapping.setUseTrailingSlashMatch(useTrailingSlashMatch);
//        }
//        if (pathConfig.getPathPrefixes() != null) {
//            mapping.setPathPrefixes(pathConfig.getPathPrefixes());
//        }

        return mapping;
    }

    @Bean
    public ApiServlet apiServlet(AdapterApiProperties adapterApiProperties) {
        return new ApiServlet(adapterApiProperties.getApiPrefix());
    }

//    @Bean("adpaterApi")
//    public SimpleServletHandlerAdapter requestMappingHandlerAdapter () {
//        return new SimpleServletHandlerAdapter();
//    }

//    @Bean
//    public ApiConfig configApiConfig () {
//        return new ApiConfig("/api");
//    }

//    @Bean
//    public ApiMappingConfig apiMappingConfig() {
//        return new ApiMappingConfig();
//    }

}
