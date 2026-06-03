package com.zincoid.nullbot.core.service.file.impl;

import com.zincoid.nullbot.core.service.file.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BoundedInputStream;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.OssService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public ResponseEntity<?> getResourceByPath(HttpServletRequest request, String path) {
        String baseDir = storageProperties.getFileDirectory();
        String fullPath = baseDir + path;
        int index = fullPath.lastIndexOf("/");
        String directory = fullPath.substring(0, index);
        String filename = fullPath.substring(index + 1);
        List<FilePO> files = fileService.search(filename, directory);
        if (files.isEmpty()) {
            log.warn("[OssService] 文件未找到 - path={}", path);
            return ResponseEntity.notFound().build();
        }
        if (files.size() > 1) {
            log.warn("[OssService] 文件路径不唯一 - path={}", path);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (files.getFirst().getIsDir() == 1) {
            log.warn("[OssService] 目标是文件夹 - path={}", path);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return getResource(request, files.getFirst());
    }

    @Override
    public ResponseEntity<?> getResourceById(HttpServletRequest request, Integer id) {
        FilePO file = fileService.getById(id);
        if (file == null) {
            log.warn("[OssService] 文件记录未找到 - id={}", id);
            return ResponseEntity.notFound().build();
        }
        if (file.getIsDir() == 1) {
            log.warn("[OssService] 目标是文件夹 - id={}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return getResource(request, file);
    }

    // =========================================== 工具方法 ===========================================

    public ResponseEntity<?> getResource(HttpServletRequest request, @NonNull FilePO file) {
        try {
            // 1. 构建安全路径
            Path rootPath = Paths.get(storageProperties.getFileDirectory()).toAbsolutePath().normalize();
            Path filePath = rootPath.resolve(Paths.get(file.getDirectory(), file.getFileName())).normalize();
            if (!filePath.startsWith(rootPath)) {
                log.warn("[OssService] 安全检查未通过 - id={}, path={}", file.getId(), filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                log.warn("[OssService] 文件无法访问 - id={}, path={}", file.getId(), filePath);
                return ResponseEntity.notFound().build();
            }

            // 2. 准备 Resource 和 Content-Type
            FileSystemResource resource = new FileSystemResource(filePath);
            MediaType mediaType = determineMediaType(file.getFileName());
            long contentLength = resource.contentLength();

            // 3. HEAD 请求直接返回头信息
            if (HttpMethod.HEAD.matches(request.getMethod())) {
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .contentLength(contentLength)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                        .build();
            }

            // 4. 处理 Range 请求
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            if (rangeHeader != null && (mediaType.getType().equals("video") || mediaType.getType().equals("audio"))) {
                return handleRangeRequest(resource, mediaType, rangeHeader, contentLength);
            }

            // 5. 完整文件响应（使用 ContentDisposition 安全设置文件名，防止中文乱码异常）
            ContentDisposition contentDisposition = ContentDisposition.inline()
                    .filename(file.getFileName(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                    .body(resource);

        } catch (Exception e) {
            log.error("[OssService] 响应失败 - id={}, path={}", file.getId(), file.getPath(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 处理 Range 请求，返回指定字节范围的 InputStreamResource
     */
    private ResponseEntity<?> handleRangeRequest(
            Resource resource, MediaType mediaType, String rangeHeader, long contentLength) {

        try {
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.size() != 1) {
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .contentLength(contentLength)
                        .body(resource);
            }

            HttpRange range = ranges.getFirst();
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);

            if (start < 0 || start > end || end >= contentLength) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                        .build();
            }

            long regionLength = end - start + 1;

            InputStream inputStream = new FileInputStream(resource.getFile());
            inputStream.skip(start);
            BoundedInputStream boundedStream = BoundedInputStream.builder()
                    .setInputStream(inputStream)
                    .setMaxCount(regionLength)
                    .get();
            InputStreamRangeResource rangeResource = new InputStreamRangeResource(boundedStream, regionLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentLength(regionLength)
                    .body(rangeResource);

        } catch (Exception e) {
            log.warn("[OssService] 无效的范围请求 - {}", rangeHeader, e);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }
    }

    /**
     * InputStreamResource 子类，覆盖 contentLength() 避免 ResourceHttpMessageConverter
     * 读取整个流来获取长度。
     */
    private static class InputStreamRangeResource extends InputStreamResource {
        private final long contentLength;

        InputStreamRangeResource(InputStream inputStream, long contentLength) {
            super(inputStream);
            this.contentLength = contentLength;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }
    }

    /**
     * 确定 Content-Type，使用 Spring 的 MediaTypeFactory
     */
    private MediaType determineMediaType(String fileName) {
        return MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}