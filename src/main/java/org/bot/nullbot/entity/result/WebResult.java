package org.bot.nullbot.entity.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebResult {

    private Integer code;
    private String message;
    private HashMap<String, Object> data = new HashMap<>();

    public static WebResult success() {
        WebResult webResult = new WebResult();
        webResult.setCode(1);
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
        return webResult;
    }

    public static WebResult fail(String message) {
        WebResult webResult = new WebResult();
        webResult.setCode(0);
        webResult.setMessage(message);
        return webResult;
    }

    public WebResult withMsg(String message) {
        this.setMessage(message);
        return this;
    }

    public WebResult withData(String key, Object value) {
        this.getData().put(key, value);
        return this;
    }
}
