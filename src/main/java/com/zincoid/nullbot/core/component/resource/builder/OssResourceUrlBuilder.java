package com.zincoid.nullbot.core.component.resource.builder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.properties.file.ResourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "file.resource.mode", havingValue = "oss")
public class OssResourceUrlBuilder implements ResourceUrlBuilder {

    private final ResourceProperties resourceProperties;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        log.info("▽ [OssResourceUrlBuilder] 资源链接构建器已初始化 - Mode: oss");
    }

    @Override
    public String from(Integer fileId) {
        return resourceProperties.getOssBaseUrl() + "/oss/" + fileId;
    }

    @Override
    public String from(String filePath) {
        String baseDir = storageProperties.getFileDirectory();
        if (!filePath.startsWith(baseDir))
            throw new IllegalArgumentException("路径不在根下");
        String relativePath = filePath.substring(baseDir.length());
        if (relativePath.isEmpty())
            throw new IllegalArgumentException("路径不能为空");
        return resourceProperties.getOssBaseUrl() + "/oss/to/" + relativePath;
    }
}
