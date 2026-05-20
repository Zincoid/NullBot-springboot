package com.zincoid.nullbot.core.component.tool;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.properties.ApiProperties;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
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
        if (!path.startsWith(baseDir))
            throw new IllegalArgumentException("路径不在根下");
        String relativePath = path.substring(baseDir.length());
        if (relativePath.isEmpty())
            throw new IllegalArgumentException("路径不能为空");
        return apiProperties.getBaseUrl() + "/oss/to/" + relativePath;
    }
}
