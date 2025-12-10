package org.bot.nullbot.plugin.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
    /**
     * 获取文件夹的树形结构字符串
     * @param rootPath 根目录路径
     * @param maxDepth 最大深度，0表示不限制
     * @return 树形结构字符串
     */
    public static String getFolderTreeString(String rootPath, int maxDepth) throws IOException {
        Path root = Paths.get(rootPath);
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(root)) {
            return "路径不存在: " + rootPath;
        }
        if (!Files.isDirectory(root)) {
            return "不是文件夹: " + rootPath;
        }
        // 添加根目录
        sb.append(root.getFileName().toString()).append("\n");
        // 构建树形结构
        buildTreeString(root, "", true, sb, maxDepth, 0);
        return sb.toString();
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
                // 当前行的前缀
                sb.append(prefix);
                if (isLast) {
                    // sb.append("└── ");
                    sb.append("└ ");
                } else {
                    // sb.append("├── ");
                    sb.append("├ ");
                }
                sb.append(item.getFileName()).append("\n");
                // 子项的前缀
                String childPrefix = prefix + (isLast ? "    " : "│   ");
                // 递归处理子文件夹
                buildTreeString(item, childPrefix, itemIsLast, sb, maxDepth, currentDepth + 1);
            }
        }
    }

    /**
     * 获取目录下所有文件名列表（带扩展名），每行一个
     * @param directoryPath 目录路径
     * @return 文件名列表字符串
     */
    public static String getFileListAsString(String directoryPath) {
        try {
            Path directory = Paths.get(directoryPath);
            // 检查目录
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return "目录不存在或不是有效目录: " + directoryPath;
            }
            // 使用NIO获取文件列表
            List<String> fileNames = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            if (fileNames.isEmpty()) {
                return "目录中没有文件: " + directoryPath;
            }
            // 用换行符连接
            return String.join("\n", fileNames);
        } catch (IOException e) {
            return "读取目录出错: " + e.getMessage();
        }
    }

    /**
     * 使用NIO从目录随机选择文件（已修改）
     * @param directoryPath 目录路径
     * @return 随机文件的完整路径
     */
    public static String getRandomFile(String directoryPath) {
        try {
            Path directory = Paths.get(directoryPath);
            // 检查目录是否存在
            if (!Files.exists(directory)) {
                throw new IllegalArgumentException("目录不存在: " + directoryPath);
            }
            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("路径不是目录: " + directoryPath);
            }
            // 使用Files.list获取文件流
            List<Path> files = Files.list(directory)
                    .filter(Files::isRegularFile)  // 只选择普通文件
                    .collect(Collectors.toList());
            if (files.isEmpty()) {
                // System.err.println("目录中没有文件: " + directoryPath);
                return null;
            }
            // 随机选择
            Random random = new Random();
            int randomIndex = random.nextInt(files.size());
            Path selectedFile = files.get(randomIndex);
            // 转换为绝对路径
            return selectedFile.toAbsolutePath().toString();
        } catch (IOException e) {
            // System.err.println("读取目录出错: " + e.getMessage());
            // return null;
            throw new IllegalArgumentException("读取目录出错: " + directoryPath);
        }
    }

    /**
     * 使用通配符过滤文件
     * @param directoryPath 目录路径
     * @param pattern 通配符模式（如 "*.{jpg,png,gif}"）
     * @return 随机文件的完整路径
     */
    public static String getRandomFileByPattern(String directoryPath, String pattern) {
        try {
            Path directory = Paths.get(directoryPath);
            // 创建PathMatcher
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            // 获取匹配的文件
            List<Path> matchedFiles = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))  // 匹配文件名
                    .collect(Collectors.toList());
            if (matchedFiles.isEmpty()) {
                System.err.println("没有匹配的文件: " + pattern);
                return null;
            }
            Random random = new Random();
            int randomIndex = random.nextInt(matchedFiles.size());
            return matchedFiles.get(randomIndex).toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 递归获取目录及其子目录中的随机文件
     * @param directoryPath 目录路径
     * @param recursive 是否递归搜索子目录
     * @return 随机文件的完整路径
     */
    public static String getRandomFileRecursive(String directoryPath, boolean recursive) {
        try {
            Path startDir = Paths.get(directoryPath);
            List<Path> allFiles = new ArrayList<>();
            if (recursive) {
                // 递归查找所有文件
                Files.walk(startDir)
                        .filter(Files::isRegularFile)
                        .forEach(allFiles::add);
            } else {
                // 仅当前目录
                Files.list(startDir)
                        .filter(Files::isRegularFile)
                        .forEach(allFiles::add);
            }
            if (allFiles.isEmpty()) {
                System.err.println("没有找到文件");
                return null;
            }
            Random random = new Random();
            int randomIndex = random.nextInt(allFiles.size());
            return allFiles.get(randomIndex).toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 使用NIO根据文件名查找文件路径
     * @param directoryPath 目录路径
     * @param fileName 要查找的文件名（带扩展名）
     * @return 文件的完整路径，如果未找到则返回null
     */
    public static String getFilePathByName(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return null;
            }
            // 使用Files.list流式处理
            Optional<Path> foundFile = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst();
            return foundFile.map(Path::toAbsolutePath).map(Path::toString).orElse(null);
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 使用通配符模式查找文件
     * @param directoryPath 目录路径
     * @param pattern 通配符模式（如 "*.jpg", "report*.pdf"）
     * @return 匹配的文件路径列表
     */
    public static List<String> getFilesByPattern(String directoryPath, String pattern) {
        List<String> foundPaths = new ArrayList<>();
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return foundPaths;
            }
            // 创建PathMatcher
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            // 查找匹配的文件
            try (Stream<Path> stream = Files.list(directory)) {
                foundPaths = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
        }
        return foundPaths;
    }

    /**
     * 使用NIO递归查找文件路径
     * @param directoryPath 目录路径
     * @param fileName 要查找的文件名（带扩展名）
     * @return 文件的完整路径，如果未找到则返回null
     */
    public static String getFilePathRecursive(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.err.println("目录不存在或不是有效目录: " + directoryPath);
                return null;
            }
            // 使用Files.walk递归查找
            try (Stream<Path> stream = Files.walk(directory)) {
                Optional<Path> foundFile = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst();
                return foundFile.map(Path::toAbsolutePath).map(Path::toString).orElse(null);
            }
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除文件（使用NIO，支持更多选项）
     * @param directoryPath 目录路径
     * @param fileName 文件名（带扩展名）
     * @return 删除结果信息
     */
    public static String deleteFileByName(String directoryPath, String fileName) {
        try {
            Path baseDir = Paths.get(directoryPath).toRealPath();
            Path targetFile = baseDir.resolve(fileName).normalize();
            // 双重验证：规范化 + 真实路径检查
            Path realTarget = targetFile.toRealPath();
            if (!realTarget.startsWith(baseDir)) {
                return "错误: 路径遍历攻击被阻止 - " + fileName;
            }
            // 检查文件是否在允许的目录内
            if (Files.isRegularFile(realTarget)) {

                // Scanner scanner = new Scanner(System.in);
                // System.out.print("确认删除此文件吗？(输入 'yes' 确认): ");
                // String input = scanner.nextLine().trim();
                // scanner.close();
                //
                // if (!input.equalsIgnoreCase("yes")) {
                //     return "取消：删除操作已取消";
                // }

                Files.delete(realTarget);
                return "已删除: " + fileName;
            }else
                return "错误: 文件不是普通文件, 无法删除";
        } catch (IOException e) {
            return "错误: 文件不存在或访问被拒绝";
        }
    }

    /**
     * 递归删除目录及其子目录中的指定文件（NIO版本）（不安全）
     * @param directoryPath 目录路径
     * @param fileName 文件名（带扩展名）
     * @return 删除结果统计
     */
    public static String deleteFileRecursive(String directoryPath, String fileName) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return "错误：目录不存在或不是目录";
            }
            // 使用Files.walk递归查找所有匹配的文件
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.walk(directory)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .collect(Collectors.toList());
            }
            if (filesToDelete.isEmpty()) {
                return "提示：没有找到文件 '" + fileName + "'";
            }
            // 显示将要删除的文件
            System.out.println("将删除以下 " + filesToDelete.size() + " 个文件:");
            for (int i = 0; i < filesToDelete.size(); i++) {
                Path file = filesToDelete.get(i);
                System.out.printf("%3d. %s\n", i + 1, file.toAbsolutePath());
            }

            // 执行删除
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

    /**
     * 删除匹配特定模式的文件（NIO版本，使用PathMatcher）（已修改，非通用方法，用于回复删除图片功能）
     * @param directoryPath 目录路径
     * @param pattern 通配符模式（如 "*.tmp", "temp_*.*"）
     * @return 删除结果统计
     */
    public static String deleteFilesByPattern(String directoryPath, String pattern) {
        try {
            // 获取基础目录的真实路径并进行安全验证
            Path baseDir = Paths.get(directoryPath).toRealPath();
            if (!Files.isDirectory(baseDir)) {
                return "错误：路径不是目录";
            }

            // 使用PathMatcher进行模式匹配
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

            // 收集匹配的文件
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.list(baseDir)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .collect(Collectors.toList());
            }

            if (filesToDelete.isEmpty()) {
                return "错误: 文件不存在或访问被拒绝";
                // return "提示：没有找到匹配模式 '" + pattern + "' 的文件";
            }

            // 执行删除（带安全验证）
            int successCount = 0;
            int failCount = 0;
            List<String> failedFiles = new ArrayList<>();
            List<String> securityRejectedFiles = new ArrayList<>();
            List<String> successFiles = new ArrayList<>(); // 新增：记录成功删除的文件名

            for (Path file : filesToDelete) {
                try {
                    // 安全验证：规范化路径 + 真实路径检查
                    Path targetFile = baseDir.resolve(file.getFileName()).normalize();
                    Path realTarget = targetFile.toRealPath();

                    // 验证真实路径是否在基础目录内（防止路径遍历攻击）
                    if (!realTarget.startsWith(baseDir)) {
                        securityRejectedFiles.add(file.getFileName().toString());
                        continue;
                    }

                    Files.delete(realTarget);
                    successCount++;
                    successFiles.add(file.getFileName().toString()); // 记录成功删除的文件名
                } catch (IOException e) {
                    failCount++;
                    failedFiles.add(file.getFileName() + " (" + e.getMessage() + ")");
                }
            }

            StringBuilder result = new StringBuilder();

            // result.append("删除完成：成功 ").append(successCount)
            //         .append(" 个，失败 ").append(failCount).append(" 个\n");

            if (!securityRejectedFiles.isEmpty()) {
                result.append("安全阻止：").append(securityRejectedFiles.size())
                        .append(" 个文件因路径安全问题被阻止删除: ")
                        .append(String.join(", ", securityRejectedFiles)).append("\n");
            }

            if(successCount == 1) {
                result.append("已删除: ");
                result.append(String.join(", ", successFiles));
            }
            else
                result.append("错误: 删除失败");

            // if (!successFiles.isEmpty()) {
            //     result.append("成功删除的文件：").append(String.join(", ", successFiles)).append("\n");
            // }

            // if (failCount > 0) {
            //     result.append("删除失败的文件：").append(String.join(", ", failedFiles));
            // }

            return result.toString().trim();
        } catch (IOException e) {
            return "错误：读取目录时发生IO异常 - " + e.getMessage();
        }
    }
}
