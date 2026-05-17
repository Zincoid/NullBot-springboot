package org.bot.nullbot.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.mapper.FileMapper;
import org.bot.nullbot.service.OssService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final FileStorageProperties fileStorageProperties;
    private final FileMapper fileMapper;

    @Override
    public ResponseEntity<?> getResource(Integer id, HttpServletRequest request) {
        try {
            // 1. 查询文件记录
            FilePO file = fileMapper.selectById(id);
            if (file == null) {
                log.warn("File record not found for id: {}", id);
                return ResponseEntity.notFound().build();
            }

            // 2. 构建安全路径
            Path rootPath = Paths.get(fileStorageProperties.getFileDirectory()).toAbsolutePath().normalize();
            Path filePath = rootPath.resolve(Paths.get(file.getDirectory(), file.getFileName())).normalize();
            if (!filePath.startsWith(rootPath)) {
                log.error("Attempt to access file outside base dir: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                log.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 3. 准备 Resource 和 Content-Type
            FileSystemResource resource = new FileSystemResource(filePath);
            MediaType mediaType = determineMediaType(file.getFileName());
            long contentLength = resource.contentLength();

            // 4. HEAD 请求直接返回头信息
            if (HttpMethod.HEAD.matches(request.getMethod())) {
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .contentLength(contentLength)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                        .build();
            }

            // 5. 处理 Range 请求
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            if (rangeHeader != null && (mediaType.getType().equals("video") || mediaType.getType().equals("audio"))) {
                return handleRangeRequest(resource, mediaType, rangeHeader, contentLength);
            }

            // 6. 完整文件响应
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to serve file id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 使用 Spring HttpRange + ResourceRegion 处理范围请求
     */
    private ResponseEntity<?> handleRangeRequest(
            Resource resource, MediaType mediaType, String rangeHeader, long contentLength) {

        try {
            // 解析 Range 头，只支持单个范围
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.size() != 1) {
                // 不支持多范围，直接返回完整文件
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .contentLength(contentLength)
                        .body(resource);
            }

            HttpRange range = ranges.getFirst();
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);

            // 安全性校验
            if (start < 0 || start > end || end >= contentLength) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                        .build();
            }

            long regionLength = end - start + 1;
            ResourceRegion region = new ResourceRegion(resource, start, regionLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentLength(regionLength)
                    .body(region);

        } catch (Exception e) {
            log.warn("Invalid Range request: {}", rangeHeader, e);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }
    }

    /**
     * 确定 Content-Type，建议使用 Spring 的 MediaTypeFactory（更全面）
     */
    private MediaType determineMediaType(String fileName) {
        // Spring 内置工具，支持更多格式
        return MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}