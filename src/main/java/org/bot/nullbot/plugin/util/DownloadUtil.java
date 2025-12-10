package org.bot.nullbot.plugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class DownloadUtil
{
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtil.class);

    /**
     * 下载文件（支持图片、视频、音频等）
     */
    public static String downloadFile(String fileUrl, String savePath, String fileName) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求头，模拟浏览器访问
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept", "*/*");

            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时

            // 自动处理重定向
            connection.setInstanceFollowRedirects(true);

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Failed: HTTP error code " + responseCode;
            }

            // 获取文件类型和大小
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();
            logger.info("Downloading: {}", fileUrl);
            logger.info("Content-Type: {}", contentType);
            if (contentLength > 0) {
                logger.info("File Size: {}", formatFileSize(contentLength));
            }

            // 如果文件名已经包含扩展名，则使用原文件名
            String finalFileName;
            if (hasExtension(fileName)) {
                finalFileName = fileName;
                logger.info("Using provided filename with extension: {}", finalFileName);
                System.out.println();
            } else {
                // 获取正确的文件扩展名
                String fileExtension = getFileExtension(contentType, fileUrl, fileName);
                finalFileName = fileName + fileExtension;
                logger.info("Added extension to filename: {}", finalFileName);
            }

            Path saveFilePath = Paths.get(savePath, finalFileName);

            // 确保目录存在
            Files.createDirectories(Paths.get(savePath));

            // 下载文件
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, saveFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 验证文件是否下载成功
            long downloadedSize = Files.size(saveFilePath);
            logger.info("Download completed: {} ({})", finalFileName, formatFileSize(downloadedSize));

            return finalFileName;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }

    /**
     * 检查文件名是否包含扩展名
     */
    private static boolean hasExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 查找最后一个点号的位置
        int lastDotIndex = fileName.lastIndexOf('.');
        int lastSlashIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        // 点号必须在最后一个斜杠之后，且不能是第一个字符或最后一个字符
        if (lastDotIndex > lastSlashIndex &&
                lastDotIndex > 0 &&
                lastDotIndex < fileName.length() - 1) {

            String extension = fileName.substring(lastDotIndex + 1);

            // 扩展名应该是1-10个字母数字字符
            return extension.length() >= 1 &&
                    extension.length() <= 10 &&
                    extension.matches("[a-zA-Z0-9]+");
        }

        return false;
    }

    /**
     * 根据Content-Type、URL和文件名获取文件扩展名
     * 优先级：文件名 > Content-Type > URL
     */
    private static String getFileExtension(String contentType, String fileUrl, String fileName) {
        // 1. 首先尝试从文件名中提取扩展名
        if (fileName != null && !fileName.isEmpty()) {
            String extensionFromFileName = extractExtensionFromFileName(fileName);
            if (!extensionFromFileName.isEmpty()) {
                logger.info("Extension from filename: {}", extensionFromFileName);
                return extensionFromFileName;
            }
        }

        // 2. 如果文件名没有扩展名，尝试从Content-Type判断
        if (contentType != null && !contentType.isEmpty()) {
            String extensionFromContentType = getExtensionFromContentType(contentType);
            if (!extensionFromContentType.isEmpty()) {
                logger.info("Extension from Content-Type: {}", extensionFromContentType);
                return extensionFromContentType;
            }
        }

        // 3. 如果Content-Type也没有，尝试从URL中提取扩展名
        if (fileUrl != null && !fileUrl.isEmpty()) {
            String extensionFromUrl = extractExtensionFromUrl(fileUrl);
            if (!extensionFromUrl.isEmpty()) {
                logger.info("Extension from URL: {}", extensionFromUrl);
                return extensionFromUrl;
            }
        }

        // 4. 默认扩展名（根据情况选择）
        logger.info("Using default extension: .dat");
        return ".dat";
    }

    /**
     * 从文件名中提取扩展名
     */
    private static String extractExtensionFromFileName(String fileName) {
        // 移除路径部分，只保留文件名
        String simpleName = new File(fileName).getName();

        int lastDotIndex = simpleName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < simpleName.length() - 1) {
            String extension = simpleName.substring(lastDotIndex).toLowerCase();

            // 验证扩展名是否合理
            if (isValidExtension(extension)) {
                return extension;
            }
        }

        return "";
    }

    /**
     * 从Content-Type获取扩展名
     */
    private static String getExtensionFromContentType(String contentType) {
        String lowerContentType = contentType.toLowerCase();

        // 图片类型
        if (lowerContentType.startsWith("image/")) {
            if (lowerContentType.contains("jpeg") || lowerContentType.contains("jpg")) return ".jpg";
            if (lowerContentType.contains("png")) return ".png";
            if (lowerContentType.contains("gif")) return ".gif";
            if (lowerContentType.contains("webp")) return ".webp";
            if (lowerContentType.contains("bmp")) return ".bmp";
            if (lowerContentType.contains("svg")) return ".svg";
            if (lowerContentType.contains("tiff")) return ".tiff";
            return ".jpg";
        }

        // 视频类型
        else if (lowerContentType.startsWith("video/")) {
            if (lowerContentType.contains("mp4")) return ".mp4";
            if (lowerContentType.contains("mpeg")) return ".mpeg";
            if (lowerContentType.contains("ogg") || lowerContentType.contains("ogv")) return ".ogv";
            if (lowerContentType.contains("webm")) return ".webm";
            if (lowerContentType.contains("avi")) return ".avi";
            if (lowerContentType.contains("quicktime") || lowerContentType.contains("mov")) return ".mov";
            if (lowerContentType.contains("x-flv")) return ".flv";
            if (lowerContentType.contains("matroska")) return ".mkv";
            return ".mp4";
        }

        // 音频类型
        else if (lowerContentType.startsWith("audio/")) {
            if (lowerContentType.contains("mpeg") || lowerContentType.contains("mp3")) return ".mp3";
            if (lowerContentType.contains("ogg")) return ".ogg";
            if (lowerContentType.contains("wav")) return ".wav";
            if (lowerContentType.contains("webm")) return ".weba";
            if (lowerContentType.contains("aac")) return ".aac";
            if (lowerContentType.contains("flac")) return ".flac";
            if (lowerContentType.contains("x-m4a")) return ".m4a";
            return ".mp3";
        }

        // 其他常见类型
        else if (lowerContentType.contains("application/")) {
            if (lowerContentType.contains("pdf")) return ".pdf";
            if (lowerContentType.contains("zip")) return ".zip";
            if (lowerContentType.contains("x-rar-compressed") || lowerContentType.contains("rar")) return ".rar";
            if (lowerContentType.contains("x-tar")) return ".tar";
            if (lowerContentType.contains("x-gzip") || lowerContentType.contains("gzip")) return ".gz";
            if (lowerContentType.contains("x-7z-compressed")) return ".7z";
            if (lowerContentType.contains("msword") || lowerContentType.contains("word")) return ".doc";
            if (lowerContentType.contains("vnd.ms-excel") || lowerContentType.contains("excel")) return ".xls";
            if (lowerContentType.contains("vnd.ms-powerpoint") || lowerContentType.contains("powerpoint")) return ".ppt";
            if (lowerContentType.contains("octet-stream")) return ".bin";
        }

        // 文本类型
        else if (lowerContentType.startsWith("text/")) {
            if (lowerContentType.contains("plain")) return ".txt";
            if (lowerContentType.contains("html")) return ".html";
            if (lowerContentType.contains("css")) return ".css";
            if (lowerContentType.contains("javascript") || lowerContentType.contains("js")) return ".js";
            if (lowerContentType.contains("xml")) return ".xml";
            if (lowerContentType.contains("csv")) return ".csv";
            return ".txt";
        }

        // JSON
        else if (lowerContentType.contains("json")) {
            return ".json";
        }

        return "";
    }

    /**
     * 从URL中提取扩展名
     */
    private static String extractExtensionFromUrl(String fileUrl) {
        // 移除查询参数和片段标识符
        String urlWithoutQuery = fileUrl.split("[?#]")[0];

        int lastDotIndex = urlWithoutQuery.lastIndexOf('.');
        int lastSlashIndex = urlWithoutQuery.lastIndexOf('/');

        if (lastDotIndex > lastSlashIndex && lastDotIndex < urlWithoutQuery.length() - 1) {
            String extension = urlWithoutQuery.substring(lastDotIndex).toLowerCase();

            // 验证扩展名是否合理
            if (isValidExtension(extension)) {
                return extension;
            }
        }

        return "";
    }

    /**
     * 验证扩展名是否有效
     */
    private static boolean isValidExtension(String extension) {
        if (extension == null || extension.length() < 2 || extension.length() > 10) {
            return false;
        }

        // 检查扩展名格式（以点开头，后跟字母数字，可能包含连字符）
        return Pattern.matches("^\\.[a-zA-Z0-9\\-]{1,9}$", extension);
    }

    /**
     * 常见扩展名白名单（可选，用于额外验证）
     */
    private static boolean isInExtensionWhitelist(String extension) {
        String[] whitelist = {
                // 图片
                ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".tiff", ".tif",
                // 视频
                ".mp4", ".mpeg", ".mpg", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm", ".ogv",
                // 音频
                ".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a", ".wma",
                // 文档
                ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf",
                // 压缩文件
                ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2",
                // 其他
                ".html", ".htm", ".css", ".js", ".json", ".xml", ".csv"
        };

        for (String ext : whitelist) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }

        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 使用HttpURLConnection下载图片
     */
    @Deprecated
    public static String downloadImage(String imageUrl, String savePath, String fileName) {
        String fullPath = savePath + "/" + fileName;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置请求头，模拟浏览器访问
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Failed: " + responseCode;
            }
            // 获取文件类型
            String contentType = connection.getContentType();
            String fileExtension = getFileExtension(contentType);
            // 生成保存路径
            Path saveFilePath = Paths.get(fullPath + fileExtension);
            // 确保目录存在
            Files.createDirectories(saveFilePath.getParent());
            // 下载文件
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, saveFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName + fileExtension;
            // return saveFilePath.toString();  // 完整路径
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: Unknown error";
        }
    }

    /**
     * 根据Content-Type获取文件扩展名
     */
    @Deprecated
    private static String getFileExtension(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".jpg";
        };
    }
}
