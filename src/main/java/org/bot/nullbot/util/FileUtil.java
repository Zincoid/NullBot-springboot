package org.bot.nullbot.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil
{
    // =================== 文件列表相关 ===================

    public static String getFolderTreeString(String rootPath, int maxDepth) throws IOException {
        Path root = Paths.get(rootPath);
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(root)) {
            return "路径不存在: " + rootPath;
        }
        if (!Files.isDirectory(root)) {
            return "不是文件夹: " + rootPath;
        }
        sb.append(root.getFileName().toString()).append("\n");
        buildTreeString(root, "", true, sb, maxDepth, 0);
        return sb.toString().trim();
    }

    private static void buildTreeString(Path directory, String prefix, boolean isLast,
                                        StringBuilder sb, int maxDepth, int currentDepth)
            throws IOException {
        if (maxDepth > 0 && currentDepth >= maxDepth) {
            return;
        }

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
                buildTreeString(item, childPrefix, itemIsLast, sb, maxDepth, currentDepth + 1);
            }
        }
    }

    public static String getFileListAsString(String directoryPath, String delimiter, boolean withExtension) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return "目录不存在或不是有效目录: " + directoryPath;
            }

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

                if (fileNames.isEmpty()) {
                    return "目录中没有文件: " + directoryPath;
                }
                return String.join(delimiter, fileNames);
            }
        } catch (IOException e) {
            return "读取目录出错: " + e.getMessage();
        }
    }

    // =================== 路径获取相关 ===================

    public static String getRandomFile(String directoryPath) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory)) {
                throw new IllegalArgumentException("目录不存在: " + directoryPath);
            }
            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("路径不是目录: " + directoryPath);
            }

            List<Path> files;
            try (Stream<Path> stream = Files.list(directory)) {
                files = stream.filter(Files::isRegularFile).toList();
            }

            if (files.isEmpty()) {
                return null;
            }

            Random random = new Random();
            Path selectedFile = files.get(random.nextInt(files.size()));
            return selectedFile.toAbsolutePath().toString();

        } catch (IOException e) {
            throw new IllegalArgumentException("读取目录出错: " + directoryPath);
        }
    }

    public static String getRandomFileByPattern(String directoryPath, String pattern) {
        try {
            Path directory = Paths.get(directoryPath);
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

            List<Path> matchedFiles;
            try (Stream<Path> stream = Files.list(directory)) {
                matchedFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .toList();
            }

            if (matchedFiles.isEmpty()) {
                System.err.println("没有匹配的文件: " + pattern);
                return null;
            }

            return matchedFiles.get(new Random().nextInt(matchedFiles.size()))
                    .toAbsolutePath().toString();

        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    public static String getRandomFileRecursive(String directoryPath, boolean recursive) {
        try {
            Path startDir = Paths.get(directoryPath);
            List<Path> allFiles = new ArrayList<>();

            if (recursive) {
                try (Stream<Path> stream = Files.walk(startDir)) {
                    stream.filter(Files::isRegularFile).forEach(allFiles::add);
                }
            } else {
                try (Stream<Path> stream = Files.list(startDir)) {
                    stream.filter(Files::isRegularFile).forEach(allFiles::add);
                }
            }

            if (allFiles.isEmpty()) {
                System.err.println("没有找到文件");
                return null;
            }

            return allFiles.get(new Random().nextInt(allFiles.size()))
                    .toAbsolutePath().toString();

        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    public static String getFilePathByName(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return null;
            }

            try (Stream<Path> stream = Files.list(directory)) {
                Optional<Path> found = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst();

                return found.map(p -> p.toAbsolutePath().toString()).orElse(null);
            }
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    public static List<String> getFilesByPattern(String directoryPath, String pattern) {
        List<String> foundPaths = new ArrayList<>();
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return foundPaths;
            }

            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

            try (Stream<Path> stream = Files.list(directory)) {
                foundPaths = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .map(path -> path.toAbsolutePath().toString())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
        }
        return foundPaths;
    }

    public static List<String> getFilesByKeyword(String directoryPath, String keyword) {
        List<String> foundPaths = new ArrayList<>();
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return foundPaths;
            }

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
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
        }
        return foundPaths;
    }

    public static String getFilePathRecursive(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return null;
            }

            try (Stream<Path> stream = Files.walk(directory)) {
                Optional<Path> found = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst();

                return found.map(p -> p.toAbsolutePath().toString()).orElse(null);
            }

        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    // =================== 文件删除相关 ===================

    public static String deleteFileByName(String directoryPath, String fileName) {
        try {
            Path baseDir = Paths.get(directoryPath).toRealPath();
            Path targetFile = baseDir.resolve(fileName).normalize();
            Path realTarget = targetFile.toRealPath();

            if (!realTarget.startsWith(baseDir)) {
                return "错误: 路径遍历攻击被阻止 - " + fileName;
            }

            if (Files.isRegularFile(realTarget)) {
                Files.delete(realTarget);
                return "已删除！\n" + fileName;
            } else {
                return "错误: 文件不是普通文件, 无法删除";
            }
        } catch (IOException e) {
            return "错误: 文件不存在或访问被拒绝";
        }
    }

    public static String deleteFileRecursive(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return "错误：目录不存在或不是目录";
            }

            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.walk(directory)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .toList();
            }

            if (filesToDelete.isEmpty()) {
                return "提示：没有找到文件 '" + fileName + "'";
            }

            System.out.println("将删除以下 " + filesToDelete.size() + " 个文件:");
            for (int i = 0; i < filesToDelete.size(); i++) {
                System.out.printf("%3d. %s\n", i + 1, filesToDelete.get(i).toAbsolutePath());
            }

            int successCount = 0;
            int failCount = 0;
            List<String> failedFiles = new ArrayList<>();

            for (Path file : filesToDelete) {
                try {
                    Files.delete(file);
                    successCount++;
                } catch (IOException e) {
                    failCount++;
                    failedFiles.add(file.toAbsolutePath() + " (" + e.getMessage() + ")");
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("删除完成：成功 ").append(successCount)
                    .append(" 个，失败 ").append(failCount).append(" 个\n");

            if (failCount > 0) {
                result.append("失败的文件：\n").append(String.join("\n", failedFiles));
            }
            return result.toString();

        } catch (IOException e) {
            return "错误：读取目录时发生IO异常 - " + e.getMessage();
        }
    }

    public static String deleteFilesByPattern(String directoryPath, String pattern) {
        try {
            Path baseDir = Paths.get(directoryPath).toRealPath();
            if (!Files.isDirectory(baseDir)) {
                return "错误：路径不是目录";
            }

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
                return "错误: 文件不存在或访问被拒绝";
            }

            int successCount = 0;
            int failCount = 0;
            List<String> securityRejectedFiles = new ArrayList<>();
            List<String> successFiles = new ArrayList<>();

            for (Path file : filesToDelete) {
                try {
                    Path targetFile = baseDir.resolve(file.getFileName()).normalize();
                    Path realTarget = targetFile.toRealPath();

                    if (!realTarget.startsWith(baseDir)) {
                        securityRejectedFiles.add(file.getFileName().toString());
                        continue;
                    }

                    Files.delete(realTarget);
                    successCount++;
                    successFiles.add(file.getFileName().toString());

                } catch (IOException e) {
                    failCount++;
                }
            }

            StringBuilder result = new StringBuilder();

            if (!securityRejectedFiles.isEmpty()) {
                result.append("安全阻止：").append(securityRejectedFiles.size())
                        .append(" 个文件因路径安全问题被阻止删除: ")
                        .append(String.join(", ", securityRejectedFiles)).append("\n");
            }

            if (successCount == 1) {
                result.append("已删除！\n").append(String.join(", ", successFiles));
            } else {
                result.append("错误: 删除出错");
            }

            return result.toString().trim();

        } catch (IOException e) {
            return "错误：读取目录时发生IO异常 - " + e.getMessage();
        }
    }
}
