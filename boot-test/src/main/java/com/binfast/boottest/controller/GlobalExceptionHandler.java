package com.binfast.boottest.controller;

import com.binfast.adpter.core.factory.GlobalExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局的的异常拦截器
 *
 * @author liuBin
 * @date 2018/09/09
 */
@Component
public class GlobalExceptionHandler implements GlobalExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理内部警告异常
     */
    @ExceptionHandler(PlatformException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result handlePlatformException(PlatformException e) {
        logger.error("系统警告：{}",e.getMessage());
        if(e.getCode() == null) return Result.fail(e.getMessage());
        return Result.fail(e.getCode(),e.getMessage());
    }

}
