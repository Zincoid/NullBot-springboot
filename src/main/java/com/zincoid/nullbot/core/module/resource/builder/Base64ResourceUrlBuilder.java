package com.zincoid.nullbot.core.module.resource.builder;

import com.zincoid.nullbot.core.mapper.FileMapper;
import com.zincoid.nullbot.core.model.data.po.FilePO;
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

    private final FileMapper fileMapper;

    @PostConstruct
    public void init() {
        log.info("▽ [Base64ResourceUrlBuilder] 资源链接构建器已初始化 - Mode: base64");
    }

    @Override
    public String from(Integer fileId) {
        FilePO file = fileMapper.selectById(fileId);
        if (file == null)
            throw new IllegalArgumentException("文件不存在: " + fileId);
        return "base64://" + Base64Util.from(file.getPath());
    }

    @Override
    public String from(String path) {
        return "base64://" + Base64Util.from(path);
    }
}
