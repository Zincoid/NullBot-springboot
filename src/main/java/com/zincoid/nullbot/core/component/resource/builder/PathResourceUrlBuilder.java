package com.zincoid.nullbot.core.component.resource.builder;

import com.zincoid.nullbot.core.mapper.FileMapper;
import com.zincoid.nullbot.core.model.data.po.FilePO;
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

    private final FileMapper fileMapper;

    @PostConstruct
    public void init() {
        log.info("▽ [PathResourceUrlBuilder] 资源链接构建器已初始化 - Mode: path");
    }

    @Override
    public String from(Integer fileId) {
        FilePO file = fileMapper.selectById(fileId);
        if (file == null)
            throw new IllegalArgumentException("文件不存在: " + fileId);
        return "file://" + file.getPath();
    }

    @Override
    public String from(String filePath) {
        return "file://" + filePath;
    }
}
