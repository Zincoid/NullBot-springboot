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
     * 替换模板中的图片
     * @param template 原始 HTML
     * @param images 替换 MAP
     * @return 处理后的 HTML
     * 占位格式: ${imageName}
     */
    public static String replaceImages(String template, Map<String, String> images) {
        String result = template;
        for (Map.Entry<String, String> entry : images.entrySet()) {
            String placeholder = entry.getKey();
            String value = entry.getValue();
            if (value == null) continue;
            // 直接匹配 ${placeholder} 格式
            String placeholderPattern = "\\$\\{" + Pattern.quote(placeholder) + "}";
            // 处理图片路径: 如果是本地文件 转换为 file:// URL
            String finalValue = value;
            if (!value.startsWith("http") && !value.startsWith("data:")) {
                File file = new File(value);
                if (file.exists()) {
                    // 转换为 file:// URL
                    finalValue = "file://" + file.getAbsolutePath().replace("\\", "/");
                } else {
                    log.info("[HtmlTemplateUtil] 文件不存在: {}", value);
                    continue; // 文件不存在时跳过替换
                }
            }
            // 替换所有匹配的占位符
            result = result.replaceAll(placeholderPattern, Matcher.quoteReplacement(finalValue));
        }
        return result;
    }

    // =================== 次要方法 ===================

    /**
     * BASE64编码图片替换 (图片直接嵌入HTML 无需外部文件)
     */
    public static String replaceImagesBase64(String template, Map<String, String> images) throws IOException {
        String result = template;
        for (Map.Entry<String, String> entry : images.entrySet()) {
            String placeholder = entry.getKey();
            String value = entry.getValue();
            if (value == null) continue;
            // 直接替换 ${placeholder} 格式的占位符
            Pattern pattern = Pattern.compile("\\$\\{" + Pattern.quote(placeholder) + "\\}");
            // 检查是否是图片文件路径（根据文件扩展名判断）
            if (value.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|svg|webp)$")) {
                File imageFile = new File(value);
                if (imageFile.exists()) {
                    // 读取图片并转换为Base64
                    String base64Image = encodeFileToBase64(imageFile);
                    String mimeType = getMimeType(value);
                    String dataUri = "data:" + mimeType + ";base64," + base64Image;
                    // 替换所有匹配项
                    result = result.replaceAll("\\$\\{" + Pattern.quote(placeholder) + "}", Matcher.quoteReplacement(dataUri));
                } else {
                    log.info("[HtmlTemplateUtil] 图片文件不存在: {}", value);
                }
            } else {
                // 如果不是图片文件，直接替换为文本值
                result = result.replaceAll("\\$\\{" + Pattern.quote(placeholder) + "}", Matcher.quoteReplacement(value));
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
