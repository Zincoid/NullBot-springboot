package org.bot.nullbot.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class Base64Util {

    private Base64Util() {}

    // ==================== 图片相关 ====================

    public static String imageToBase64(Path imagePath) {
        return fileToBase64(imagePath);
    }

    public static String imageToBase64(String imagePath) {
        return fileToBase64(Path.of(imagePath));
    }

    public static String imageToBase64(BufferedImage image) {
        return imageToBase64(image, "png");
    }

    public static String imageToBase64(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("BufferedImage转Base64失败: ", e);
        }
    }

    // ============== 通用文件 (视频 音频等) ==============

    public static String fileToBase64(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("文件转Base64失败: " + filePath, e);
        }
    }

    public static String fileToBase64(String filePath) {
        return fileToBase64(Path.of(filePath));
    }
}