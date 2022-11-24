package com.binfast.adpter.core.factory;

import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author 刘斌
 * @date 2022/11/24 8:28 上午
 */
public class ResponseFactory {
    private final ResponseProcessor responseProcessor;

    public ResponseFactory(ApplicationContext context) {
        Map<String, ResponseProcessor> beansOfType = context.getBeansOfType(ResponseProcessor.class);
        if (beansOfType.size() > 0) {
            responseProcessor = beansOfType.values().stream().findFirst().get();
        } else {
            responseProcessor = new DefaultResponseProcessor();
        }
    }

    /**
     * 构造成功结果
     */
    public Object buildSuccess(Object t) {
        return responseProcessor.buildSuccess(t);
    }

    /**
     * 构造失败结果
     */
    public Object buildFailure(String errCode, String errMessage) {
        return responseProcessor.buildFailure(errCode, errMessage);
    }

    /**
     * 构造失败结果
     */
    public Object buildFailure(Object t) {
        return t;
    }


}
