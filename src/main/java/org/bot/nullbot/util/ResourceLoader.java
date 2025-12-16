package org.bot.nullbot.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceLoader
{
    private static final Map<String, Path> CACHE = new ConcurrentHashMap<>();

    public static synchronized Path getCached(String resourcePath) throws IOException {
        // 使用缓存
        return CACHE.computeIfAbsent(resourcePath, key -> {
            try {
                return loadFromResources(key);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static Path loadFromResources(String resourcePath) throws IOException {
        // 获取资源流
        InputStream resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new FileNotFoundException("资源未找到: " + resourcePath);
        }

        // 创建临时文件
        String fileName = Paths.get(resourcePath).getFileName().toString();
        Path tempFile = Files.createTempFile("resource-", "-" + fileName);

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
