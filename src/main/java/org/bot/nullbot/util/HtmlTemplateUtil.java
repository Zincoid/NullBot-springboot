package org.bot.nullbot.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HtmlTemplateUtil
{
    // =================== 主要方法 ===================

    /**
     * 从模板文件加载并替换占位符
     * @param templatePath 模板 HTML 路径
     * @param variables 替换 MAP
     * @return 处理后的 HTML
     * 占位格式: ${variableName}
     */
    public static String loadTemplate(String templatePath, Map<String, String> variables) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(templatePath)));
        return replaceVariables(content, variables);
    }

    /**
     * 插入图片到 HTML
     * @param html 原始 HTML
     * @param imgTag 要插入的 img 标签或占位符
     * @param imagePath 图片路径（本地或网络URL）
     * @return 处理后的 HTML
     */
    public static String insertImage(String html, String imgTag, String imagePath) {
        // 如果图片是本地文件 转换为 data URI
        if (imagePath.startsWith("/") || imagePath.startsWith(".") ||
                imagePath.startsWith("file://")) {
            try {
                // 读取图片并转换为 base64
                File imageFile = new File(imagePath.replace("file://", ""));
                if (imageFile.exists()) {
                    String base64Image = encodeFileToBase64(imageFile);
                    String mimeType = getMimeType(imagePath);
                    String dataUri = "data:" + mimeType + ";base64," + base64Image;

                    // 替换占位符或添加 img 标签
                    if (html.contains(imgTag)) {
                        return html.replace(imgTag, "<img src=\"" + dataUri + "\" style=\"max-width: 100%;\">");
                    } else {
                        return html.replace("</body>", "<img src=\"" + dataUri + "\" style=\"max-width: 100%;\"></body>");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 转换失败 使用原始路径
                return html.replace(imgTag, "<img src=\"" + imagePath + "\" style=\"max-width: 100%;\">");
            }
        }
        // 网络图片或无法转换的图片
        return html.replace(imgTag, "<img src=\"" + imagePath + "\" style=\"max-width: 100%;\">");
    }

    // =================== 次要方法 ===================

    /**
     * 替换模板中的变量
     * @param template 模板 HTML 字符串
     * @param variables 替换 MAP
     * @return 处理后的 HTML
     * 占位格式: ${variableName}
     */
    private static String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "\\$\\{" + entry.getKey() + "\\}";
            result = result.replaceAll(placeholder,
                    entry.getValue() != null ? entry.getValue() : "");
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
