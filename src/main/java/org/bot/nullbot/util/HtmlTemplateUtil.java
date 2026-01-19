package org.bot.nullbot.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HtmlTemplateUtil
{
    // =================== 主要方法 ===================

    /**
     * 从模板文件加载并替换占位符
     * @param templatePath 模板 HTML 路径
     * @return HTML 字符串
     */
    public static String loadTemplate(String templatePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(templatePath)));
    }

    /**
     * 替换模板中的变量
     * @param template 模板 HTML 字符串
     * @param variables 替换 MAP
     * @return 处理后的 HTML
     * 占位格式: ${variableName}
     */
    public static String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "\\$\\{" + entry.getKey() + "}";
            result = result.replaceAll(placeholder,
                    entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    /**
     * 插入图片到 HTML
     * @param template 原始 HTML
     * @param images 替换 MAP
     * @return 处理后的 HTML
     * 占位格式: ${imageName}
     */
    public static String replaceImages(String template, Map<String, String> images) {
        String result = template;
        for (Map.Entry<String, String> entry : images.entrySet()) {
            String placeholder = entry.getKey();
            String imagePath = entry.getValue();
            // 模式1: 直接替换 ${placeholder}
            String placeholderPattern = "src\\s*=\\s*[\"']\\$\\{" + Pattern.quote(placeholder) + "}[\"']";
            // String placeholderPattern = "\\$\\{" + Pattern.quote(placeholder) + "}[\"']";
            // 模式2: 已经转换的 src 路径
            String finalImagePath = imagePath;
            // 如果是本地文件 转换为文件URL
            if (imagePath != null && !imagePath.startsWith("http") && !imagePath.startsWith("data:")) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    // 转换为 file:// URL
                    finalImagePath = "file://" + imageFile.getAbsolutePath().replace("\\", "/");
                } else {
                    log.info("[HtmlTemplateUtil] 图片文件不存在: {}", imagePath);
                    continue;
                }
            }
            // 替换占位符为实际图片路径
            String replacement = "src=\"" + finalImagePath + "\"";
            result = result.replaceAll(placeholderPattern, replacement);
        }
        return result;
    }

    // =================== 次要方法 ===================

    /**
     * BASE64编码图片替换 (图片直接嵌入HTML 无需外部文件)
     */
    public static String replaceImagesWithBase64(String template, Map<String, String> images) throws IOException {
        String result = template;
        for (Map.Entry<String, String> entry : images.entrySet()) {
            String placeholder = entry.getKey();
            String imagePath = entry.getValue();
            if (imagePath == null) continue;
            // 查找 src="${placeholder}" 模式
            Pattern pattern = Pattern.compile(
                    "src\\s*=\\s*[\"']\\$\\{" + Pattern.quote(placeholder) + "}[\"']",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    log.info("[HtmlTemplateUtil] 图片文件不存在: {}", imagePath);
                    continue;
                }
                // 读取图片并转换为Base64
                String base64Image = encodeFileToBase64(imageFile);
                String mimeType = getMimeType(imagePath);
                String dataUri = "data:" + mimeType + ";base64," + base64Image;
                // 替换所有匹配项
                result = result.replaceAll(
                        "src\\s*=\\s*[\"']\\$\\{" + Pattern.quote(placeholder) + "}[\"']",
                        "src=\"" + dataUri + "\""
                );
            }
        }
        return result;
    }

    // =================== 工具方法 ===================

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private static String getMimeType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "image/png"; // 默认
    }
}
