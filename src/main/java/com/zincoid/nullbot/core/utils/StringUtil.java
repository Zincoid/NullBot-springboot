package com.zincoid.nullbot.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class StringUtil {

    private StringUtil() {}

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
        int availableLength = Math.max(1, maxLength - extension.length() - ellipsis.length());
        // 保留文件名前部 直接加省略号和扩展名
        String truncated = nameWithoutExt.substring(0, Math.min(availableLength, nameWithoutExt.length())) + ellipsis;
        return truncated + extension;
    }

    public static String getFolderTreeString(String rootPath, int maxDepth) throws IOException {
        Path root = Path.of(rootPath);
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + rootPath);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(root.getFileName().toString()).append("\n");
        buildTreeString(root, "", sb, maxDepth, 0);
        return sb.toString().trim();
    }

    private static void buildTreeString(Path directory, String prefix, StringBuilder sb,
                                        int maxDepth, int currentDepth) throws IOException {
        if (maxDepth > 0 && currentDepth >= maxDepth) return;
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> items = stream
                    .filter(Files::isDirectory)
                    .sorted((p1, p2) -> p1.getFileName().toString()
                            .compareToIgnoreCase(p2.getFileName().toString()))
                    .toList();

            for (int i = 0; i < items.size(); i++) {
                Path item = items.get(i);
                boolean itemIsLast = (i == items.size() - 1);

                sb.append(prefix);
                sb.append(itemIsLast ? "└ " : "├ ");
                sb.append(item.getFileName()).append("\n");
                String childPrefix = prefix + (itemIsLast ? "    " : "│ ");

                buildTreeString(item, childPrefix, sb, maxDepth, currentDepth + 1);
            }
        }
    }
}
