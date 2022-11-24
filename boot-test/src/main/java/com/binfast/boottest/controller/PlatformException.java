package com.binfast.boottest.controller;

/**
 * 平台异常统一处理
 * 重要：业务异常不会生成堆栈信息（覆盖了生成堆栈的方法）
 * @author admin
 */
public class PlatformException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    public PlatformException() {
        super();
    }


    public PlatformException(String message) {
        super(message);
    }

    public PlatformException(String message, Throwable e){
        super(message,e);
    }

    public PlatformException(Throwable e) {
        super(e);
    }

    public PlatformException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
