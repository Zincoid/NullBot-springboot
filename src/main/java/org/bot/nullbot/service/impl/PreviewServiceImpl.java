package org.bot.nullbot.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.HttpRangeResource;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.mapper.FileMapper;
import org.bot.nullbot.service.OssService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewServiceImpl implements OssService {

    private final FileMapper fileMapper;

    // =================== WEB功能相关 ===================

    @Override
    public ResponseEntity<@NonNull Resource> getResource(Integer id, HttpServletRequest request) {
        try {
            // 查询文件信息
            FilePO file = fileMapper.selectById(id);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            Path filePath = Paths.get(file.getDirectory(), file.getFileName())
                    .normalize();

            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // 创建Resource对象
            Resource resource = new UrlResource(filePath.toUri());

            // 确定Content-Type
            String contentType = determineContentType(file.getFileName());

            // 处理范围请求
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            if (rangeHeader != null && (contentType.startsWith("video/") || contentType.startsWith("audio/"))) {
                return handleRangeRequest(filePath, resource, contentType, rangeHeader);
            }

            // 返回完整文件
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + file.getFileName() + "\"")
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.of(ChronoUnit.HOURS))) // 缓存1小时
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 处理范围请求（视频支持）
     */
    private ResponseEntity<@NonNull Resource> handleRangeRequest(
            Path filePath, Resource resource,
            String contentType, String rangeHeader) throws IOException {

        long fileSize = Files.size(filePath);

        // 解析范围请求
        String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;

        if (rangeEnd >= fileSize) {
            rangeEnd = fileSize - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;

        // 创建范围资源
        Resource rangeResource = new HttpRangeResource(resource, rangeStart, rangeEnd);

        // 返回部分内容响应
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(contentLength)
                .body(rangeResource);
    }

    /**
     * 确定Content-Type
     */
    private String determineContentType(String fileName) {
        String lowerName = fileName.toLowerCase();

        // 常见图片格式
        Map<String, String> imageTypes = Map.of(
                ".jpg", "image/jpeg",
                ".jpeg", "image/jpeg",
                ".png", "image/png",
                ".gif", "image/gif",
                ".bmp", "image/bmp",
                ".webp", "image/webp",
                ".svg", "image/svg+xml"
        );

        // 常见视频格式
        Map<String, String> videoTypes = Map.of(
                ".mp4", "video/mp4",
                ".webm", "video/webm",
                ".ogg", "video/ogg",
                ".mov", "video/quicktime",
                ".avi", "video/x-msvideo",
                ".mkv", "video/x-matroska",
                ".flv", "video/x-flv",
                ".wmv", "video/x-ms-wmv"
        );

        // 常见音频格式
        Map<String, String> audioTypes = Map.of(
                ".mp3", "audio/mpeg",
                ".wav", "audio/wav",
                ".flac", "audio/flac",
                ".aac", "audio/aac",
                ".ogg", "audio/ogg",
                ".m4a", "audio/mp4",
                ".wma", "audio/x-ms-wma",
                ".opus", "audio/opus"
        );

        // 检查文件扩展名
        for (Map.Entry<String, String> entry : imageTypes.entrySet()) {
            if (lowerName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        for (Map.Entry<String, String> entry : videoTypes.entrySet()) {
            if (lowerName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        for (Map.Entry<String, String> entry : audioTypes.entrySet()) {
            if (lowerName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 默认类型
        return "application/octet-stream";
    }
}
