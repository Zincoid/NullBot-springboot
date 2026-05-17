package org.bot.nullbot.component.tool;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.prop.ApiProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OssUrlBuilder {

    private final ApiProperties apiProperties;

    public String from(Integer fileId) {
        return apiProperties.getBaseUrl() + "/oss/" + fileId;
    }
}
