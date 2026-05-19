package org.bot.nullbot.util;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.info.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;


@Slf4j
public final class DownloadUtil {

    private DownloadUtil() {}

    /**
     * 主下载方法 (无 LOG 前缀)
     */
    public static FileInfo downloadFile(String fileUrl, String savePath, String fileName) {
        return downloadFile(fileUrl, savePath, fileName, "▽ [DownloadUtil] ");
    }

    /**
     * 主下载方法 (自定义 LOG 前缀)
     */
    public static FileInfo downloadFile(String fileUrl, String savePath, String fileName, String logPrefix) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();

            setCommonHeaders(connection);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                drainStream(connection.getErrorStream());
                throw new RuntimeException("Failed: HTTP error code " + responseCode);
            }

            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();

            final long MAX_FILE_SIZE = 500L * 1024 * 1024;
            if (contentLength > MAX_FILE_SIZE) {
                log.warn("{}File too large: {} > {}", logPrefix, formatFileSize(contentLength), formatFileSize(MAX_FILE_SIZE));
                throw new RuntimeException("Failed: File too large");
            }

            log.info("{}Downloading from url...", logPrefix);
            log.info("{}Content-Type: {}", logPrefix, contentType);
            if (contentLength > 0) {
                log.info("{}File Size: {}", logPrefix, formatFileSize(contentLength));
            }

            String finalFileName = determineFileName(fileName, contentType, fileUrl, logPrefix);
            Path saveFilePath = Paths.get(savePath, finalFileName);
            Files.createDirectories(Paths.get(savePath));

            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(saveFilePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        log.warn("{}Download interrupted by thread", logPrefix);
                        Files.deleteIfExists(saveFilePath);
                        throw new RuntimeException("Failed: Download interrupted");
                    }

                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (contentLength > 0 && totalBytesRead % (10 * 1024 * 1024) == 0) {
                        log.info("{}Download progress: {}/{}", logPrefix, formatFileSize(totalBytesRead), formatFileSize(contentLength));
                    }
                }

                long downloadedSize = Files.size(saveFilePath);
                log.info("{}Download completed: {} ({})", logPrefix, finalFileName, formatFileSize(downloadedSize));

                LocalDateTime lastModified = Files
                        .getLastModifiedTime(saveFilePath)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                return new FileInfo(finalFileName, downloadedSize, lastModified);
            }

        } catch (IOException e) {
            log.error("{}Download failed: {}", logPrefix, e.getMessage(), e);
            throw new RuntimeException("Failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("{}Unexpected error: {}", logPrefix, e.getMessage(), e);
            throw new RuntimeException("Failed: Unexpected error");
        } finally {
            closeConnection(connection, logPrefix);
        }
    }

    // =============================
    // 工具
    // =============================

    private static void setCommonHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Accept", "*/*");
    }

    private static void drainStream(InputStream stream) {
        if (stream == null) return;
        try (InputStream s = stream) {
            byte[] buffer = new byte[2048];
            while (s.read(buffer) > 0) {
                // drain
            }
        } catch (IOException ignored) {
        }
    }

    private static void closeConnection(HttpURLConnection connection, String logPrefix) {
        if (connection == null) return;
        try {
            try {
                drainStream(connection.getInputStream());
            } catch (Exception ignored) {
            }
        } finally {
            try {
                connection.disconnect();
            } catch (Exception e) {
                log.warn("{}Failed to disconnect connection: {}", logPrefix, e.getMessage());
            }
        }
    }

    private static String determineFileName(String fileName, String contentType, String fileUrl, String logPrefix) {
        if (hasExtension(fileName)) {
            return fileName;
        }
        String extension = getFileExtension(contentType, fileUrl, fileName, logPrefix);
        return fileName + extension;
    }

    private static boolean hasExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        return !extractExtensionFromFileName(fileName).isEmpty();
    }

    private static String getFileExtension(String contentType, String fileUrl, String fileName, String logPrefix) {
        String ext;

        if (fileName != null && !fileName.isEmpty()) {
            ext = extractExtensionFromFileName(fileName);
            if (!ext.isEmpty()) {
                log.info("{}Extension from filename: {}", logPrefix, ext);
                return ext;
            }
        }

        if (contentType != null && !contentType.isEmpty()) {
            ext = getExtensionFromContentType(contentType);
            if (!ext.isEmpty()) {
                log.info("{}Extension from Content-Type: {}", logPrefix, ext);
                return ext;
            }
        }

        if (fileUrl != null && !fileUrl.isEmpty()) {
            ext = extractExtensionFromUrl(fileUrl);
            if (!ext.isEmpty()) {
                log.info("{}Extension from URL: {}", logPrefix, ext);
                return ext;
            }
        }

        log.info("{}Using default extension: .dat", logPrefix);
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
                && Pattern.matches("^\\.[a-zA-Z0-9\\-]{1,9}$", ext);
    }

    private static String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int idx = (int) (Math.log10(size) / Math.log10(1024));
        idx = Math.min(idx, units.length - 1);

        return String.format("%.2f %s", size / Math.pow(1024, idx), units[idx]);
    }
}
