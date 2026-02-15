package org.bot.nullbot.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtil
{
    // =================== 文件列表相关 ===================

    public static String getFolderTreeString(String rootPath, int maxDepth) throws IOException {
        Path root = Paths.get(rootPath);
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + rootPath);
        }
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

    public static String getFileListAsString(String directoryPath, String delimiter, boolean withExtension) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            try (Stream<Path> stream = Files.list(directory)) {
                List<String> fileNames = stream
                        .filter(Files::isRegularFile)
                        .map(path -> {
                            String fileName = path.getFileName().toString();
                            if (!withExtension) {
                                int dotIndex = fileName.lastIndexOf('.');
                                if (dotIndex > 0)
                                    return fileName.substring(0, dotIndex);
                            }
                            return fileName;
                        })
                        .collect(Collectors.toList());

                if (fileNames.isEmpty()) return "无文件";
                return String.join(delimiter, fileNames);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    // =================== 路径获取相关 ===================

    public static String getRandomFile(String directoryPath) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            List<Path> files;
            try (Stream<Path> stream = Files.list(directory)) {
                files = stream.filter(Files::isRegularFile).toList();
            }
            if (files.isEmpty()) return null;
            return files.get(new Random().nextInt(files.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static String getRandomFileByPattern(String directoryPath, String pattern) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            List<Path> matchedFiles;
            try (Stream<Path> stream = Files.list(directory)) {
                matchedFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .toList();
            }
            if (matchedFiles.isEmpty()) return null;
            return matchedFiles.get(new Random().nextInt(matchedFiles.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static String getRandomFileRecursive(String directoryPath) {
        Path startDir = Paths.get(directoryPath);
        if (!Files.exists(startDir) || !Files.isDirectory(startDir)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            List<Path> files;
            try (Stream<Path> stream = Files.walk(startDir)) {
                files = stream.filter(Files::isRegularFile).toList();
            }
            if (files.isEmpty()) return null;
            return files.get(new Random().nextInt(files.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static String getFilePathByName(String directoryPath, String fileName) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            try (Stream<Path> stream = Files.list(directory)) {
                Optional<Path> found = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst();
                return found.map(p -> p.toAbsolutePath().toString()).orElse(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static List<String> getFilesByPattern(String directoryPath, String pattern) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        List<String> foundPaths;
        try {
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            try (Stream<Path> stream = Files.list(directory)) {
                foundPaths = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .map(path -> path.toAbsolutePath().toString())
                        .collect(Collectors.toList());
            }
            return foundPaths;
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static List<String> getFilesByKeyword(String directoryPath, String keyword) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        List<String> foundPaths;
        try {
            String lowerKeyword = keyword.toLowerCase();  // 不区分大小写的关键字匹配
            try (Stream<Path> stream = Files.list(directory)) {
                foundPaths = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return fileName.contains(lowerKeyword);
                        })
                        .map(path -> path.toAbsolutePath().toString())
                        .collect(Collectors.toList());
            }
            return foundPaths;
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static String getFilePathRecursive(String directoryPath, String fileName) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            try (Stream<Path> stream = Files.walk(directory)) {
                Optional<Path> found = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst();
                return found.map(p -> p.toAbsolutePath().toString()).orElse(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    // =================== 文件删除相关 ===================

    public static void deleteFileByName(String directoryPath, String fileName) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            Path baseDir = directory.toRealPath();
            Path targetFile = baseDir.resolve(fileName).normalize();
            Path realTarget = targetFile.toRealPath();
            if (!realTarget.startsWith(baseDir)) {
                throw new SecurityException("路径遍历攻击被阻止: " + fileName);
            }
            if (Files.isRegularFile(realTarget)) {
                Files.delete(realTarget);
            } else {
                throw new IllegalArgumentException("不是普通文件");
            }
        } catch (IOException e) {
            throw new RuntimeException("文件不存在或访问被拒绝: " + e.getMessage());
        }
    }

    public static int deleteFileRecursive(String directoryPath, String fileName) {  // 不安全
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.walk(directory)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .toList();
            }
            if (filesToDelete.isEmpty()) {
                throw new IllegalArgumentException("文件不存在");
            }

            int deleteCount = 0;
            for (Path file : filesToDelete) {
                try {
                    Files.delete(file);
                    deleteCount++;
                } catch (IOException ignored) {
                }
            }
            return deleteCount;
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }

    public static int deleteFilesByPattern(String directoryPath, String pattern) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        try {
            Path baseDir = directory.toRealPath();
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.list(baseDir)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .toList();
            }
            if (filesToDelete.isEmpty()) {
                throw new IllegalArgumentException("文件不存在");
            }
            int deleteCount = 0;
            List<String> securityRejectedFiles = new ArrayList<>();
            for (Path file : filesToDelete) {
                try {
                    Path targetFile = baseDir.resolve(file.getFileName()).normalize();
                    Path realTarget = targetFile.toRealPath();
                    if (!realTarget.startsWith(baseDir)) {
                        securityRejectedFiles.add(file.getFileName().toString());
                        continue;
                    }
                    Files.delete(realTarget);
                    deleteCount++;
                } catch (IOException ignored) {
                }
            }
            if (!securityRejectedFiles.isEmpty()) {
                log.info("安全阻止: {} 个文件删除因路径安全问题被阻止\n{}", securityRejectedFiles.size(), String.join(", ", securityRejectedFiles));
            }
            return deleteCount;
        } catch (IOException e) {
            throw new RuntimeException("读取目录出错: " + e.getMessage());
        }
    }
}
