package org.bot.qqbot.plugin.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadUtil
{
    /**
     * 使用HttpURLConnection下载图片
     */
    public static String downloadImage(String imageUrl, String savePath, String filename) {
        String fullPath = savePath + "/" + filename;
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
            return saveFilePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: Unknown error";
        }
    }

    /**
     * 根据Content-Type获取文件扩展名
     */
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
