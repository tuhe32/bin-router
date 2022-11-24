package com.binfast.adpter.core.factory;

import com.alibaba.cola.dto.Response;

/**
 * @author 刘斌
 * @date 2022/11/24 10:24 上午
 */

public class DefaultResponseProcessor implements ResponseProcessor{

    @Override
    public Object buildSuccess(Object t) {
        return t;
    }

    @Override
    public Object buildFailure(String errCode, String errMessage) {
        return Response.buildFailure(errCode, errMessage);
    }

}
