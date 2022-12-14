package com.binfast.boottest.controller;

import com.binfast.adpter.core.factory.ResponseProcessor;
import org.springframework.stereotype.Component;

/**
 * @author εζ
 * @date 2022/11/24 10:29 δΈε
 */
@Component
public class ResponseTest implements ResponseProcessor {

    @Override
    public Object buildSuccess(Object t) {
        return Result.ok(t);
    }

    @Override
    public Object buildFailure(String errCode, String errMessage) {
        return Result.fail(Integer.parseInt(errCode), errMessage);
    }
}
