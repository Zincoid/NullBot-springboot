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
    public static String downloadFile(String fileUrl, String savePath, String baseFileName) {
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

            System.out.println("Downloading: " + fileUrl);
            System.out.println("Content-Type: " + contentType);
            if (contentLength > 0) {
                System.out.println("File Size: " + formatFileSize(contentLength));
            }

            // 获取正确的文件扩展名
            String fileExtension = getFileExtension(contentType, fileUrl);

            // 构建最终文件名（包含扩展名）
            String fileNameWithExt = baseFileName + fileExtension;
            Path saveFilePath = Paths.get(savePath, fileNameWithExt);

            // 确保目录存在
            Files.createDirectories(Paths.get(savePath));

            // 下载文件
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, saveFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 验证文件是否下载成功
            long downloadedSize = Files.size(saveFilePath);
            System.out.println("Download completed: " + fileNameWithExt + " (" + formatFileSize(downloadedSize) + ")");

            return fileNameWithExt;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }

    /**
     * 下载文件（支持进度显示）
     */
    public static String downloadFileWithProgress(String fileUrl, String savePath, String baseFileName) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求头
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Failed: HTTP error code " + responseCode;
            }

            // 获取文件信息
            String contentType = connection.getContentType();
            long fileSize = connection.getContentLengthLong();

            // 获取扩展名
            String fileExtension = getFileExtension(contentType, fileUrl);
            String fileNameWithExt = baseFileName + fileExtension;
            Path saveFilePath = Paths.get(savePath, fileNameWithExt);

            // 确保目录存在
            Files.createDirectories(Paths.get(savePath));

            // 下载文件（带进度）
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(saveFilePath.toFile())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                System.out.println("Downloading: " + fileNameWithExt);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 显示下载进度
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        System.out.print("\rProgress: " + progress + "% (" +
                                formatFileSize(totalBytesRead) + " / " + formatFileSize(fileSize) + ")");
                    }
                }
                System.out.println("\nDownload completed!");
            }

            return fileNameWithExt;

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
            String lowerContentType = contentType.toLowerCase();

            // 图片类型
            if (lowerContentType.startsWith("image/")) {
                if (lowerContentType.contains("png")) return ".png";
                if (lowerContentType.contains("gif")) return ".gif";
                if (lowerContentType.contains("webp")) return ".webp";
                if (lowerContentType.contains("bmp")) return ".bmp";
                if (lowerContentType.contains("svg")) return ".svg";
                if (lowerContentType.contains("tiff")) return ".tiff";
                return ".jpg"; // 默认图片格式
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
                return ".mp4"; // 默认视频格式
            }

            // 音频类型
            else if (lowerContentType.startsWith("audio/")) {
                if (lowerContentType.contains("mpeg")) return ".mp3";
                if (lowerContentType.contains("ogg")) return ".ogg";
                if (lowerContentType.contains("wav")) return ".wav";
                if (lowerContentType.contains("webm")) return ".weba";
                if (lowerContentType.contains("aac")) return ".aac";
                if (lowerContentType.contains("flac")) return ".flac";
                if (lowerContentType.contains("x-m4a")) return ".m4a";
                return ".mp3"; // 默认音频格式
            }

            // 其他类型
            else if (lowerContentType.contains("pdf")) return ".pdf";
            else if (lowerContentType.contains("zip")) return ".zip";
            else if (lowerContentType.contains("rar")) return ".rar";
            else if (lowerContentType.contains("tar")) return ".tar";
            else if (lowerContentType.contains("gzip") || lowerContentType.contains("x-gzip")) return ".gz";
            else if (lowerContentType.contains("7z")) return ".7z";
            else if (lowerContentType.contains("text/plain")) return ".txt";
            else if (lowerContentType.contains("text/html")) return ".html";
            else if (lowerContentType.contains("text/css")) return ".css";
            else if (lowerContentType.contains("javascript")) return ".js";
            else if (lowerContentType.contains("json")) return ".json";
            else if (lowerContentType.contains("xml")) return ".xml";
        }

        // 如果无法从Content-Type判断，尝试从URL中提取扩展名
        if (fileUrl != null && !fileUrl.isEmpty()) {
            // 移除查询参数
            String urlWithoutQuery = fileUrl.split("\\?")[0];
            int lastDotIndex = urlWithoutQuery.lastIndexOf('.');
            int lastSlashIndex = urlWithoutQuery.lastIndexOf('/');

            if (lastDotIndex > lastSlashIndex && lastDotIndex < urlWithoutQuery.length() - 1) {
                String extension = urlWithoutQuery.substring(lastDotIndex);
                // 只保留合理的扩展名（1-10个字符，只包含字母数字）
                if (extension.length() <= 10 && extension.matches("\\.[a-zA-Z0-9]+")) {
                    return extension.toLowerCase();
                }
            }
        }

        // 默认扩展名
        return ".dat";
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
