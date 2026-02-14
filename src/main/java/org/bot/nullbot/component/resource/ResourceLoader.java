package org.bot.nullbot.component.resource;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ResourceLoader
{
    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    private final Map<String, Path> CACHE = new ConcurrentHashMap<>();

    // =================== 应用方法 ===================

    public Path getCached(String resourcePath) throws IOException {
        return getCached(resourcePath, fileStorageProperties.getTempPath());
    }

    public Path getCached(String resourcePath, String tempPath) throws IOException {
        // 尝试获取缓存
        Path cachedPath = getCachedOrigin(resourcePath, tempPath);
        // 检查文件存在
        if (cachedPath != null && Files.exists(cachedPath)) return cachedPath;
        // 重载更新缓存
        synchronized (CACHE) {
            // 再次检查 (双重检查锁定)
            Path currentPath = CACHE.get(resourcePath);
            if (currentPath != null && Files.exists(currentPath)) return currentPath;
            try {
                Path newPath = loadFromResources(resourcePath, tempPath);
                CACHE.put(resourcePath, newPath);
                return newPath;
            } catch (IOException e) {
                // 重载失败 清除缓存
                CACHE.remove(resourcePath);
                throw e;
            }
        }
    }

    public void clearCache() {
        CACHE.clear();
    }

    public void removeFromCache(String resourcePath) {
        CACHE.remove(resourcePath);
    }

    // =================== 工具方法 ===================

    public Path getCachedOrigin(String resourcePath, String tempPath) {
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
        // 添加至文件系统
        LocalDateTime lastModified = Files
                .getLastModifiedTime(tempFile)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        fileService.addFileRecordForBot(
                tempPath, tempFile.getFileName().toString(), Files.size(tempFile),
                lastModified, null, null
        );
        // JVM退出时删除
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}