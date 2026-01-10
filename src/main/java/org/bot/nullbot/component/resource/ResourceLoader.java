package org.bot.nullbot.component.resource;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ResourceLoader
{
    private final FileService fileService;
    private final Map<String, Path> CACHE = new ConcurrentHashMap<>();

    public Path getCached(String resourcePath, String tempPath) throws IOException {
        // 使用缓存
        return CACHE.computeIfAbsent(resourcePath, key -> {
            try {
                return loadFromResources(key, tempPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public Path loadFromResources(String resourcePath, String tempPath) throws IOException {
        Path tempDir = Paths.get(tempPath);
        // 确保目录存在
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);
        // 获取资源流
        InputStream resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) throw new FileNotFoundException("资源未找到: " + resourcePath);
        // 创建临时文件
        String fileName = Paths.get(resourcePath).getFileName().toString();
        Path tempFile = Files.createTempFile(tempDir, "resource-", "-" + fileName);
        // 资源复制
        try (OutputStream out = Files.newOutputStream(tempFile)) {
            resourceStream.transferTo(out);
        }
        resourceStream.close();
        // JVM退出时删除
        tempFile.toFile().deleteOnExit();
        // 添加至文件系统
        fileService.addFileRecordForBot(
                tempPath, fileName, Files.size(tempFile),
                LocalDateTime.now(), null, null
        );
        return tempFile;
    }

    public void clearCache() {
        CACHE.clear();
    }

    public void removeFromCache(String resourcePath) {
        CACHE.remove(resourcePath);
    }
}