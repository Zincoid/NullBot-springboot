package com.zincoid.nullbot.core.model.result;

import lombok.Data;

@Data
public class WebResult<T> {

    // 状态常量
    public static final int SUCCESS_CODE = 1;
    public static final int FAIL_CODE = 0;
    // 消息常量
    public static final String SUCCESS_MSG = "success";
    public static final String FAIL_MSG = "fail";

    private Integer code;
    private String message;
    private T data;

    private WebResult() {}

    private static <T> WebResult<T> create(int code, String message, T data) {
        WebResult<T> result = new WebResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // =========== build 方法 ===========

    public static <T> WebResult<T> build(Integer code) {
        return create(code, null, null);
    }
    public static <T> WebResult<T> build(Integer code, String message) {
        return create(code, message, null);
    }
    public static <T> WebResult<T> build(Integer code, String message, T data) {
        return create(code, message, data);
    }

    // ========== success 方法 ==========

    public static <T> WebResult<T> success() {
        return create(SUCCESS_CODE, SUCCESS_MSG, null);
    }
    public static <T> WebResult<T> success(String message) {
        return create(SUCCESS_CODE, message, null);
    }
    public static <T> WebResult<T> success(T data) {
        return create(SUCCESS_CODE, SUCCESS_MSG, data);
    }
    public static <T> WebResult<T> success(String message, T data) {
        return create(SUCCESS_CODE, message, data);
    }

    // ============ fail 方法 ============

    public static <T> WebResult<T> fail() {
        return create(FAIL_CODE, FAIL_MSG, null);
    }
    public static <T> WebResult<T> fail(String message) {
        return create(FAIL_CODE, message, null);
    }
    public static <T> WebResult<T> fail(T data) {
        return create(FAIL_CODE, FAIL_MSG, data);
    }
    public static <T> WebResult<T> fail(String message, T data) {
        return create(FAIL_CODE, message, data);
    }
}