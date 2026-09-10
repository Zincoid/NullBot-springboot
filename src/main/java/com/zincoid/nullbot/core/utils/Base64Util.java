package com.zincoid.nullbot.core.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class Base64Util {

    private Base64Util() {}

    // ============== 本地图片转换 ==============

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

    // ============== 本地文件转换 ==============

    public static String from(String path) {
        return from(Path.of(path));
    }

    public static String from(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("文件转Base64失败: " + path, e);
        }
    }
}
