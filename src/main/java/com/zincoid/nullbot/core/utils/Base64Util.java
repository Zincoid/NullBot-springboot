package com.zincoid.nullbot.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public final class Base64Util {

    private Base64Util() {}

    // ============== 网络图片转换 ==============

    private static final long MAX_TOTAL = 128L * 1024 * 1024; // 缓存总量 128MB

    private static final Map<String, Optional<String>> CACHE = new LinkedHashMap<>(128, 0.75f, true);
    private static long cacheBytes = 0;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public static String dataUri(String url) {
        if (url == null || url.isBlank()) return null;
        synchronized (CACHE) {
            Optional<String> cached = CACHE.get(url);
            if (cached != null) return cached.orElse(null);
        }
        Optional<String> data;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200)
                throw new RuntimeException("HTTP " + response.statusCode());
            byte[] bytes = response.body();
            if (bytes.length == 0)
                throw new RuntimeException("空图片内容");
            String mime = sniffMime(bytes);
            if (mime == null)
                throw new RuntimeException("非图片内容");
            data = Optional.of("data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes));
        } catch (Exception e) {
            log.warn("◉ [Base64Util] 图片下载失败: {} - {}", url, e.getMessage());
            data = Optional.empty();
        }
        synchronized (CACHE) {
            if (CACHE.containsKey(url)) return data.orElse(null);
            CACHE.put(url, data);
            cacheBytes += data.map(String::length).orElse(0);
            while (cacheBytes > MAX_TOTAL && CACHE.size() > 1) {
                Iterator<Map.Entry<String, Optional<String>>> it = CACHE.entrySet().iterator();
                Map.Entry<String, Optional<String>> eldest = it.next();
                cacheBytes -= eldest.getValue().map(String::length).orElse(0);
                it.remove();
            }
        }
        return data.orElse(null);
    }

    private static String sniffMime(byte[] b) {
        if (b.length < 12) return null;
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
            return "image/jpeg";
        if ((b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47)
            return "image/png";
        if (b[0] == 0x47 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x38)
            return "image/gif";
        if (b[0] == 0x52 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x46
                && b[8] == 0x57 && b[9] == 0x45 && b[10] == 0x42 && b[11] == 0x50)
            return "image/webp";
        return null;
    }

    // ============== 本地图片转换 ==============

    public static String from(BufferedImage image) {
        return from(image, "png");
    }

    public static String from(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图片转Base64失败: ", e);
        }
    }

    // ============== 本地文件转换 ==============

    public static String from(String filePath) {
        return from(Path.of(filePath));
    }

    public static String from(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("文件转Base64失败: " + filePath, e);
        }
    }
}