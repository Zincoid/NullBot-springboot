package com.zincoid.nullbot.core.utils;

import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.information.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


@Slf4j
public final class DownloadUtil {

    private static final ThreadLocal<Set<Path>> THREAD_TEMP_FILES = ThreadLocal.withInitial(HashSet::new);

    private static final Pattern EXT_PATTERN = Pattern.compile("^\\.[a-zA-Z0-9\\-]{1,9}$");
    private static final int BUF_SIZE = 8192;
    private static final int DRAIN_BUF_SIZE = 2048;
    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024;

    private DownloadUtil() {}

    /** 清除临时文件 (需在线程结束前调用) */
    public static int cleanup() {
        Set<Path> files = THREAD_TEMP_FILES.get();
        int count = 0;
        for (Path path : files) {
            try {
                if (Files.deleteIfExists(path)) count++;
            } catch (IOException e) {
                log.warn("▽ [DownloadUtil] Failed to delete temp file: {}, {}", path, e.getMessage());
            }
        }
        files.clear();
        THREAD_TEMP_FILES.remove();
        return count;
    }

    /** 下载临时文件 */
    public static FileInfo save(String url) {
        String tempDir = System.getProperty("java.io.tmpdir");
        FileInfo fileInfo = save(url, tempDir, UUID.randomUUID().toString());
        Path filePath = Paths.get(fileInfo.getPath());
        filePath.toFile().deleteOnExit();
        THREAD_TEMP_FILES.get().add(filePath);
        return fileInfo;
    }

    /** 下载永久文件 */
    public static FileInfo save(String url, String directory, String name) {
        HttpURLConnection connection = null;
        boolean streamConsumed = false;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            setCommonHeaders(connection);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                drainStream(connection.getErrorStream());
                throw new RuntimeException("Failed: HTTP error code " + responseCode);
            }
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_FILE_SIZE) {
                log.warn("▽ [DownloadUtil] File too large: {} > {}",
                        formatFileSize(contentLength), formatFileSize(MAX_FILE_SIZE));
                throw new RuntimeException("Failed: File too large");
            }
            log.info("▽ [DownloadUtil] Downloading... Content-Type: {}, Size: {}",
                    contentType, contentLength > 0 ? formatFileSize(contentLength) : "unknown");
            String fileName = determineFileName(name, contentType, url);
            Path filePath = Paths.get(directory, fileName);
            Files.createDirectories(Paths.get(directory));
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[BUF_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long nextLogThreshold = 10 * 1024 * 1024;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        log.warn("▽ [DownloadUtil] Download interrupted by thread");
                        throw new RuntimeException("Failed: Download interrupted");
                    }
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (contentLength > 0 && totalBytesRead >= nextLogThreshold) {
                        log.info("▽ [DownloadUtil] Download progress: {}/{}",
                                formatFileSize(totalBytesRead), formatFileSize(contentLength));
                        nextLogThreshold += 10 * 1024 * 1024;
                    }
                }
                streamConsumed = true;
                long downloadedSize = Files.size(filePath);
                log.info("▽ [DownloadUtil] Download completed: {} ({})",
                        fileName, formatFileSize(downloadedSize));
                return new FileInfo(directory, fileName, downloadedSize,
                        Files.getLastModifiedTime(filePath)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime());
            } catch (IOException e) {
                Files.deleteIfExists(filePath);
                throw e;
            }
        } catch (IOException e) {
            log.error("▽ [DownloadUtil] Download failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("▽ [DownloadUtil] Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed: Unexpected error");
        } finally {
            if (connection != null) {
                if (!streamConsumed) drainStream(connection.getErrorStream());
                connection.disconnect();
            }
        }
    }

    // =============================
    // 工具
    // =============================

    private static void setCommonHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Accept", "*/*");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
    }

    private static void drainStream(InputStream stream) {
        if (stream == null) return;
        try (InputStream s = stream) {
            byte[] buffer = new byte[DRAIN_BUF_SIZE];
            while (s.read(buffer) > 0) {}  // drain
        } catch (IOException ignored) {}
    }

    private static String determineFileName(String fileName, String contentType, String fileUrl) {
        if (hasExtension(fileName)) return fileName;
        String extension = getFileExtension(contentType, fileUrl, fileName);
        return fileName + extension;
    }

    private static boolean hasExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        return !extractExtensionFromFileName(fileName).isEmpty();
    }

    private static String getFileExtension(String contentType, String fileUrl, String fileName) {
        String ext;
        if (fileName != null && !fileName.isEmpty()) {
            ext = extractExtensionFromFileName(fileName);
            if (!ext.isEmpty()) {
                log.info("▽ [DownloadUtil] Extension from filename: {}", ext);
                return ext;
            }
        }
        if (contentType != null && !contentType.isEmpty()) {
            ext = getExtensionFromContentType(contentType);
            if (!ext.isEmpty()) {
                log.info("▽ [DownloadUtil] Extension from Content-Type: {}", ext);
                return ext;
            }
        }
        if (fileUrl != null && !fileUrl.isEmpty()) {
            ext = extractExtensionFromUrl(fileUrl);
            if (!ext.isEmpty()) {
                log.info("▽ [DownloadUtil] Extension from URL: {}", ext);
                return ext;
            }
        }
        log.info("▽ [DownloadUtil] Using default extension: .dat");
        return ".dat";
    }

    private static String extractExtensionFromFileName(String fileName) {
        String simpleName = new File(fileName).getName();
        int lastDotIndex = simpleName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < simpleName.length() - 1) {
            String extension = simpleName.substring(lastDotIndex).toLowerCase();
            if (isValidExtension(extension)) return extension;
        }
        return "";
    }

    private static String getExtensionFromContentType(String contentType) {
        String lower = contentType.toLowerCase();
        if (lower.startsWith("image/")) {
            if (lower.contains("jpeg") || lower.contains("jpg")) return ".jpg";
            if (lower.contains("png")) return ".png";
            if (lower.contains("gif")) return ".gif";
            if (lower.contains("webp")) return ".webp";
            if (lower.contains("bmp")) return ".bmp";
            if (lower.contains("svg")) return ".svg";
            if (lower.contains("tiff")) return ".tiff";
            return ".jpg";
        }
        if (lower.startsWith("video/")) {
            if (lower.contains("mp4")) return ".mp4";
            if (lower.contains("mpeg")) return ".mpeg";
            if (lower.contains("og")) return ".ogv";
            if (lower.contains("webm")) return ".webm";
            if (lower.contains("avi")) return ".avi";
            if (lower.contains("mov")) return ".mov";
            if (lower.contains("flv")) return ".flv";
            if (lower.contains("mkv")) return ".mkv";
            return ".mp4";
        }
        if (lower.startsWith("audio/")) {
            if (lower.contains("mp3") || lower.contains("mpeg")) return ".mp3";
            if (lower.contains("ogg")) return ".ogg";
            if (lower.contains("wav")) return ".wav";
            if (lower.contains("webm")) return ".weba";
            if (lower.contains("aac")) return ".aac";
            if (lower.contains("flac")) return ".flac";
            if (lower.contains("m4a")) return ".m4a";
            return ".mp3";
        }
        if (lower.contains("application/")) {
            if (lower.contains("pdf")) return ".pdf";
            if (lower.contains("zip")) return ".zip";
            if (lower.contains("rar")) return ".rar";
            if (lower.contains("tar")) return ".tar";
            if (lower.contains("gzip")) return ".gz";
            if (lower.contains("7z")) return ".7z";
            if (lower.contains("word")) return ".doc";
            if (lower.contains("excel")) return ".xls";
            if (lower.contains("powerpoint")) return ".ppt";
            if (lower.contains("octet-stream")) return ".bin";
        }
        if (lower.startsWith("text/")) {
            if (lower.contains("plain")) return ".txt";
            if (lower.contains("html")) return ".html";
            if (lower.contains("css")) return ".css";
            if (lower.contains("javascript")) return ".js";
            if (lower.contains("xml")) return ".xml";
            if (lower.contains("csv")) return ".csv";
            return ".txt";
        }
        if (lower.contains("json")) return ".json";
        return "";
    }

    private static String extractExtensionFromUrl(String fileUrl) {
        String urlNoQuery = fileUrl.split("[?#]")[0];
        int lastDot = urlNoQuery.lastIndexOf('.');
        int lastSlash = urlNoQuery.lastIndexOf('/');
        if (lastDot > lastSlash && lastDot < urlNoQuery.length() - 1) {
            String ext = urlNoQuery.substring(lastDot).toLowerCase();
            if (isValidExtension(ext)) return ext;
        }
        return "";
    }

    private static boolean isValidExtension(String ext) {
        return ext != null && ext.length() >= 2 && ext.length() <= 10
                && EXT_PATTERN.matcher(ext).matches();
    }

    private static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int idx = (int) (Math.log10(size) / Math.log10(1024));
        idx = Math.min(idx, units.length - 1);
        return String.format("%.2f %s", size / Math.pow(1024, idx), units[idx]);
    }
}
