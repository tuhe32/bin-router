package com.binfast.adpter.core.factory;

/**
 * @author 刘斌
 * @date 2022/11/24 9:36 上午
 */
public interface ResponseProcessor {

    /**
     * 成功结果封装
     */
    Object buildSuccess(Object t);

    /**
     * 失败结果封装
     */
    Object buildFailure(String errCode, String errMessage);
}
