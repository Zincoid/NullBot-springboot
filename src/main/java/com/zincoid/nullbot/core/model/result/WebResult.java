package com.zincoid.nullbot.core.model.result;

import lombok.Data;

import java.util.HashMap;

@Data
public class WebResult {

    private Integer code;
    private String message;
    private HashMap<String, Object> data = new HashMap<>();

    private WebResult() {}

    @Deprecated
    public static WebResult build() {
        return new WebResult();
    }

    public static WebResult build(Integer code, String message) {
        WebResult webResult = new WebResult();
        webResult.setCode(code);
        webResult.setMessage(message);
        return webResult;
    }

    public static WebResult success() {
        WebResult webResult = new WebResult();
        webResult.setCode(1);
        webResult.setMessage("success");
        return webResult;
    }

    public static WebResult success(String message) {
        WebResult webResult = new WebResult();
        webResult.setCode(1);
        webResult.setMessage(message);
        return webResult;
    }

    public static WebResult fail() {
        WebResult webResult = new WebResult();
        webResult.setCode(0);
        webResult.setMessage("fail");
        return webResult;
    }

    public static WebResult fail(String message) {
        WebResult webResult = new WebResult();
        webResult.setCode(0);
        webResult.setMessage(message);
        return webResult;
    }

    @Deprecated
    public WebResult withCode(Integer code) {
        this.setCode(code);
        return this;
    }

    @Deprecated
    public WebResult withMsg(String message) {
        this.setMessage(message);
        return this;
    }

    public WebResult withData(String key, Object value) {
        this.getData().put(key, value);
        return this;
    }
}
