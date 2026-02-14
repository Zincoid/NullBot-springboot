package org.bot.nullbot.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class Base64Util
{
    public static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Base64转换失败 (BufferedImage 方法)");
        }
    }

    public static String imageToBase64(Path imagePath) {
        try {
            byte[] pngBytes = Files.readAllBytes(imagePath);
            return Base64.getEncoder().encodeToString(pngBytes);
        } catch (Exception e) {
            throw new RuntimeException("Base64转换失败 (Path 方法)");
        }
    }

    public static String imageToBase64(String imagePath) {
        try {
            byte[] pngBytes = Files.readAllBytes(Path.of(imagePath));
            return Base64.getEncoder().encodeToString(pngBytes);
        } catch (Exception e) {
            throw new RuntimeException("Base64转换失败 (String 方法)");
        }
    }
}
