package org.bot.nullbot.util;

public class StringUtil
{
    public static String truncateFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.length() <= maxLength) {
            return fileName;
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        String extension = "";
        String nameWithoutExt = fileName;
        // 获取扩展名
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            extension = fileName.substring(lastDotIndex);
            nameWithoutExt = fileName.substring(0, lastDotIndex);
        }
        // 计算可保留的文件名长度 (减去扩展名长度和省略符号长度)
        String ellipsis = "⋯"; // 使用垂直居中的省略号
        int availableLength = maxLength - extension.length() - ellipsis.length();
        if (availableLength <= 0) {  // 如果可用长度太小 至少保留一些字符
            availableLength = Math.max(1, maxLength - extension.length() - ellipsis.length());
        }
        // 保留文件名前部 直接加省略号和扩展名
        String truncated = nameWithoutExt.substring(0, Math.min(availableLength, nameWithoutExt.length())) + ellipsis;
        return truncated + extension;
    }
}
