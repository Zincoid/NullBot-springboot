package com.zincoid.nullbot.core.module.resource.builder;

import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.Base64Util;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "file.resource.mode", havingValue = "base64")
public class Base64ResourceUrlBuilder implements ResourceUrlBuilder {

    private final FileService fileService;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        log.info("▽ [Base64ResourceUrlBuilder] 资源链接构建器已初始化 - Mode: base64");
    }

    @Override
    public String from(Integer fileId) {
        FilePO file = fileService.getById(fileId);
        if (file == null) throw new IllegalArgumentException("文件不存在: " + fileId);
        return "base64://" + Base64Util.from(storageProperties.resolve(file.getPath()));
    }

    @Override
    public String from(String filePath) {
        return "base64://" + Base64Util.from(storageProperties.resolve(filePath));
    }
}
