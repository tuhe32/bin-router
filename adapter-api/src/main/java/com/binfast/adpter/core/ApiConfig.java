package com.binfast.adpter.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author 刘斌
 * @date 2022/11/5 9:06 下午
 */
public class ApiConfig implements BeanDefinitionRegistryPostProcessor {
    private String path;

    public ApiConfig(String path) {
        this.path = path;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        beanDefinitionRegistry.registerBeanDefinition(path, new RootBeanDefinition(ApiServlet.class));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        configurableListableBeanFactory.registerSingleton("adpaterApi", new RequestMappingHandlerAdapter());
    }
}
