package com.binfast.adpter.doc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.handler.SimpleServletHandlerAdapter;

/**
 * @author 刘斌
 * @date 2022/11/4 4:54 下午
 */
public class ApiDoc implements BeanDefinitionRegistryPostProcessor {
    String docPath;

    public ApiDoc(String docPath) {
        this.docPath = docPath;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        beanDefinitionRegistry.registerBeanDefinition(docPath, new RootBeanDefinition(ApiDocServlet.class));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        configurableListableBeanFactory.registerSingleton("apiDocAdapter", new SimpleServletHandlerAdapter());
    }
}
