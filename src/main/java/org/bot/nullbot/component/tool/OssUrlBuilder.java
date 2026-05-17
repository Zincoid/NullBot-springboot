package org.bot.nullbot.component.tool;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.prop.ApiProperties;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OssUrlBuilder {

    private final ApiProperties apiProperties;
    private final FileStorageProperties fileStorageProperties;

    public String from(Integer fileId) {
        return apiProperties.getBaseUrl() + "/oss/" + fileId;
    }

    public String from(String path) {
        String baseDir = fileStorageProperties.getFileDirectory();
        if (path.startsWith(baseDir)) {
            String relativePath = path.substring(baseDir.length());
            if (relativePath.isEmpty())
                throw new  IllegalArgumentException("无相对路径");
            return apiProperties.getBaseUrl() + "/oss/to/" + relativePath;
        }
        throw new IllegalArgumentException("路径不在文件根下");
    }
}
