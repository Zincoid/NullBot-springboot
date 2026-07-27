package com.zincoid.nullbot.core.module.resource.builder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
        return resourceProperties.getOssBaseUrl() + "/oss/to" + filePath;
    }
}
