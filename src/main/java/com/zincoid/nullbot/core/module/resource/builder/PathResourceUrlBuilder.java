package com.zincoid.nullbot.core.module.resource.builder;

import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.service.file.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "file.resource.mode", havingValue = "path", matchIfMissing = true)
public class PathResourceUrlBuilder implements ResourceUrlBuilder {

    private final FileService fileService;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        log.info("▽ [PathResourceUrlBuilder] 资源链接构建器已初始化 - Mode: path");
    }

    @Override
    public String from(Integer fileId) {
        FilePO file = fileService.getById(fileId);
        if (file == null) throw new IllegalArgumentException("文件不存在: " + fileId);
        return "file://" + storageProperties.resolve(file.getPath());
    }

    @Override
    public String from(String filePath) {
        return "file://" + storageProperties.resolve(filePath);
    }
}
