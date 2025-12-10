package org.bot.nullbot.plugin.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadUtil
{
    /**
     * 下载文件（支持图片、视频、音频等）
     */
    public static String downloadFile(String fileUrl, String savePath, String fileName) {
        String fullPath = savePath + "/" + fileName;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求头，模拟浏览器访问
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时

            // 自动处理重定向
            connection.setInstanceFollowRedirects(true);

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                    && responseCode != HttpURLConnection.HTTP_MOVED_PERM) {
                return "Failed: HTTP error code " + responseCode;
            }

            // 处理重定向
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                String newUrl = connection.getHeaderField("Location");
                return downloadFile(newUrl, savePath, fileName);
            }

            // 获取文件类型和大小
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();

            // System.out.println("Downloading: " + fileUrl);
            // System.out.println("Content-Type: " + contentType);
            // System.out.println("File Size: " + formatFileSize(contentLength));

            // 生成保存路径和扩展名
            String fileExtension = getFileExtension(contentType, fileUrl);
            String finalFileName = fileName + fileExtension;
            Path saveFilePath = Paths.get(savePath, finalFileName);

            // 确保目录存在
            Files.createDirectories(Paths.get(savePath));

            // 下载文件
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(saveFilePath.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 显示下载进度（可选）
                    if (contentLength > 0) {
                        int progress = (int) ((totalBytesRead * 100) / contentLength);
                        System.out.print("\rDownloading: " + progress + "%");
                    }
                }
                System.out.println("\nDownload completed!");
            }

            // 验证文件是否下载成功
            long downloadedSize = Files.size(saveFilePath);
            if (contentLength > 0 && downloadedSize != contentLength) {
                System.out.println("Warning: File size mismatch. Expected: " + contentLength + ", Downloaded: " + downloadedSize);
            }

            return finalFileName;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }

    /**
     * 根据Content-Type和URL获取文件扩展名
     */
    private static String getFileExtension(String contentType, String fileUrl) {
        // 首先尝试从Content-Type判断
        if (contentType != null) {
            contentType = contentType.toLowerCase();

            // 图片类型
            if (contentType.contains("image/")) {
                return switch (contentType) {
                    case "image/png" -> ".png";
                    case "image/gif" -> ".gif";
                    case "image/webp" -> ".webp";
                    case "image/bmp" -> ".bmp";
                    case "image/svg+xml" -> ".svg";
                    case "image/tiff" -> ".tiff";
                    default -> ".jpg"; // 默认图片格式
                };
            }

            // 视频类型
            else if (contentType.contains("video/")) {
                return switch (contentType) {
                    case "video/mp4" -> ".mp4";
                    case "video/mpeg" -> ".mpeg";
                    case "video/ogg" -> ".ogv";
                    case "video/webm" -> ".webm";
                    case "video/x-msvideo" -> ".avi";
                    case "video/quicktime" -> ".mov";
                    case "video/x-flv" -> ".flv";
                    case "video/x-matroska" -> ".mkv";
                    default -> ".mp4"; // 默认视频格式
                };
            }

            // 音频类型
            else if (contentType.contains("audio/")) {
                return switch (contentType) {
                    case "audio/mpeg" -> ".mp3";
                    case "audio/ogg" -> ".ogg";
                    case "audio/wav" -> ".wav";
                    case "audio/webm" -> ".weba";
                    case "audio/aac" -> ".aac";
                    case "audio/flac" -> ".flac";
                    case "audio/x-m4a" -> ".m4a";
                    case "audio/x-wav" -> ".wav";
                    default -> ".mp3"; // 默认音频格式
                };
            }

            // 其他常见文件类型
            else {
                return switch (contentType) {
                    case "application/pdf" -> ".pdf";
                    case "application/zip" -> ".zip";
                    case "application/x-rar-compressed" -> ".rar";
                    case "application/x-tar" -> ".tar";
                    case "application/gzip" -> ".gz";
                    case "application/x-7z-compressed" -> ".7z";
                    case "text/plain" -> ".txt";
                    case "text/html" -> ".html";
                    case "text/css" -> ".css";
                    case "text/javascript", "application/javascript" -> ".js";
                    case "application/json" -> ".json";
                    case "application/xml" -> ".xml";
                    default -> "";
                };
            }
        }

        // 如果无法从Content-Type判断，尝试从URL中提取扩展名
        if (fileUrl != null && !fileUrl.isEmpty()) {
            int lastDotIndex = fileUrl.lastIndexOf('.');
            int lastSlashIndex = fileUrl.lastIndexOf('/');

            if (lastDotIndex > lastSlashIndex && lastDotIndex < fileUrl.length() - 1) {
                String extension = fileUrl.substring(lastDotIndex);
                // 验证扩展名是否合理（避免包含查询参数）
                if (extension.length() <= 10 && extension.matches("\\.[a-zA-Z0-9]+")) {
                    return extension.toLowerCase();
                }
            }
        }

        // 默认扩展名（可根据需要修改）
        return ".bin";
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long size) {
        if (size <= 0) return "Unknown";

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
