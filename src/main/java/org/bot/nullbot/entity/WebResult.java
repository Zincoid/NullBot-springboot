package org.bot.nullbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebResult
{
    private Integer code;
    private String message;
    private HashMap<String, Object> data = new HashMap<>();

    public static WebResult success(){
        WebResult webResult = new WebResult();
        webResult.setCode(200);
        return webResult;
    }

    public static WebResult fail(){
        WebResult webResult = new WebResult();
        webResult.setCode(400);
        return webResult;
    }

    public WebResult addMsg(String message){
        this.setMessage(message);
        return this;
    }

    public WebResult addData(String key, Object value){
        this.getData().put(key, value);
        return this;
    }
}
