package com.zincoid.nullbot.core.module.resource.loader;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ResourceLoader {

    private final Map<String, Path> cache = new ConcurrentHashMap<>();
    private final StorageProperties storageProperties;

    public Path getCache(String resourcePath) {
        return getCache(resourcePath, storageProperties.getTempPath());
    }

    public Path getCache(String resourcePath, String tempPath) {
        Path existing = cache.get(resourcePath);
        if (existing != null && Files.exists(existing)) return existing;
        return cache.compute(resourcePath, (key, prev) -> {
            if (prev != null && Files.exists(prev)) return prev;
            try {
                Path path = loadResource(key, tempPath);
                path.toFile().deleteOnExit();
                return path;
            } catch (IOException e) {
                throw new UncheckedIOException("资源加载失败: " + key, e);
            }
        });
    }

    public void clearCache() {
        cache.clear();
    }

    public void removeCache(String resourcePath) {
        cache.remove(resourcePath);
    }

    private Path loadResource(String resourcePath, String tempPath) throws IOException {
        Path tempDir = Path.of(tempPath);
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);
        String fileName = Path.of(resourcePath).getFileName().toString();
        Path tempFile = Files.createTempFile(tempDir, "resource-", "-" + fileName);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("资源缺失: " + resourcePath);
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                in.transferTo(out);
            }
        }
        return tempFile;
    }
}