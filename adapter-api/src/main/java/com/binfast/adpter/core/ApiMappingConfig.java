package com.binfast.adpter.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author εζ
 * @date 2022/11/8 9:32 δΈε
 */
public class ApiMappingConfig extends WebMvcConfigurationSupport {

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

//    @Bean("adpaterApi")
//    public RequestMappingHandlerAdapter requestMappingHandlerAdapter () {
//        return new RequestMappingHandlerAdapter();
//    }

    @Bean
    public ApiServlet apiServlet() {
        return new ApiServlet("");
    }
}
