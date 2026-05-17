package org.bot.nullbot.component.tool;

import org.bot.nullbot.config.prop.ApiProperties;
import org.springframework.stereotype.Component;

@Component
public class OssUrlBuilder {

    private ApiProperties apiProperties;

    public static String getFileUrl(Integer fileId) {
        return "http://nullbot.zincoid.online/api/oss/" + fileId;
    }
}
