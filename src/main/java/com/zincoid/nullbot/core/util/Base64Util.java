package com.zincoid.nullbot.core.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class Base64Util {

    private Base64Util() {}

    // ============== 图片相关转换 ==============

    public static String from(BufferedImage image) {
        return from(image, "png");
    }

    public static String from(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图片转Base64失败: ", e);
        }
    }

    // ============== 通用文件转换 ==============

    public static String from(String filePath) {
        return from(Path.of(filePath));
    }

    public static String from(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("文件转Base64失败: " + filePath, e);
        }
    }
}