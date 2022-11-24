package com.binfast.boottest.controller;

import java.io.Serializable;

import static com.binfast.boottest.controller.ResultCode.*;

/**
 * 响应信息主体
 *
 * @author admin
 * @param <T>
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String msg = "success";

    private int code = 0;

    private T data;

    public Result() {
        super();
    }

    public Result(T data) {
        super();
        this.data = data;
    }

    public Result(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    public Result(Throwable e) {
        super();
        this.msg = e.getMessage();
        this.code = Integer.parseInt(FAIL.getCode());
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {return "msg='" + msg + '\'' +", code=" + code + ", data=" + data ;}

    public static <T> Result<T> fail() {
        Result<T> ret = new Result<T>();
        ret.setCode(Integer.parseInt(FAIL.getCode()));
        ret.setMsg(FAIL.getDesc());
        return ret;
    }

    public static <T>  Result<T> fail(T data) {
        Result<T> ret = Result.fail();
        ret.setData(data);
        return ret;
    }

    public static <T>  Result<T> fail(String msg) {
        Result<T> ret = Result.fail();
        ret.setMsg(msg);
        return ret;
    }


    public static <T> Result<T> fail(Integer code,String msg) {
        Result<T> ret = new Result<T>();
        ret.setCode(code);
        ret.setMsg(msg);
        return ret;
    }

    public static <T> Result<T> ok() {
        Result<T> ret = new Result<T>();
        ret.setCode(Integer.parseInt(SUCCESS.getCode()));
        ret.setMsg(SUCCESS.getDesc());
        return ret;
    }

    public static <T>  Result<T> ok(String msg) {
        Result<T> ret = Result.ok();
        ret.setMsg(msg);
        return ret;
    }

    public static <T>  Result<T> ok(T msg,Boolean isData) {
        if(!isData) return ok(msg+"");
        else return ok(msg);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> ret = Result.ok();
        ret.setData(data);
        return ret;
    }

    public static <T> Result<T>  http404(T data) {
        Result<T> ret = new Result<T>();
        ret.setCode(Integer.parseInt(NOT_FOUND.getCode()));
        ret.setMsg(NOT_FOUND.getDesc());
        ret.setData(data);
        return ret;
    }

    public static <T> Result<T> http403(T data) {
        Result<T> ret = new Result<T>();
        ret.setCode(Integer.parseInt(ACCESS_ERROR.getCode()));
        ret.setMsg(ACCESS_ERROR.getDesc());
        ret.setData(data);
        return ret;
    }
}
