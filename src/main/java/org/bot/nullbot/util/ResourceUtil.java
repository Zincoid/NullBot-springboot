package org.bot.nullbot.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public final class ResourceUtil  // 改用 Spring 组件 ResourceLoader
{
    private static final Map<String, Path> CACHE = new ConcurrentHashMap<>();

    private ResourceUtil() {}

    public static synchronized Path getCached(String resourcePath, String tempPath) throws IOException {
        // 使用缓存
        return CACHE.computeIfAbsent(resourcePath, key -> {
            try {
                return loadFromResources(key, tempPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static Path loadFromResources(String resourcePath, String tempPath) throws IOException {
        Path tempDir = Paths.get(tempPath);

        // 确保目录存在
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // 获取资源流
        InputStream resourceStream = ResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new FileNotFoundException("资源未找到: " + resourcePath);
        }

        // 创建临时文件
        String fileName = Paths.get(resourcePath).getFileName().toString();
        Path tempFile = Files.createTempFile(tempDir, "resource-", "-" + fileName);

        // 将资源复制到临时文件
        try (OutputStream out = Files.newOutputStream(tempFile)) {
            resourceStream.transferTo(out);
        }

        resourceStream.close();

        // 设置为JVM退出时删除
        tempFile.toFile().deleteOnExit();

        return tempFile;
    }
}
