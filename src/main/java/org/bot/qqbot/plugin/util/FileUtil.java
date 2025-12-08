package org.bot.qqbot.plugin.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
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
     * 使用NIO从目录随机选择文件
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
                System.err.println("目录中没有文件: " + directoryPath);
                return null;
            }
            // 随机选择
            Random random = new Random();
            int randomIndex = random.nextInt(files.size());
            Path selectedFile = files.get(randomIndex);
            // 转换为绝对路径
            return selectedFile.toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("读取目录出错: " + e.getMessage());
            return null;
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

            // // 确认删除
            // Scanner scanner = new Scanner(System.in);
            // System.out.print("确认删除这些文件吗？(输入 'yes' 确认): ");
            // String input = scanner.nextLine().trim();
            //
            // if (!input.equalsIgnoreCase("yes")) {
            //     return "取消：删除操作已取消";
            // }

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
     * 删除匹配特定模式的文件（NIO版本，使用PathMatcher）（不安全）
     * @param directoryPath 目录路径
     * @param pattern 通配符模式（如 "*.tmp", "temp_*.*"）
     * @return 删除结果统计
     */
    public static String deleteFilesByPattern(String directoryPath, String pattern) {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return "错误：目录不存在或不是目录";
            }
            // 使用PathMatcher进行模式匹配
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
            // 收集匹配的文件
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.list(directory)) {
                filesToDelete = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .collect(Collectors.toList());
            }
            if (filesToDelete.isEmpty()) {
                return "提示：没有找到匹配模式 '" + pattern + "' 的文件";
            }
            // 显示将要删除的文件
            System.out.println("将删除以下 " + filesToDelete.size() + " 个文件:");
            for (int i = 0; i < filesToDelete.size(); i++) {
                System.out.printf("%3d. %s\n", i + 1, filesToDelete.get(i).getFileName());
            }

            // // 确认删除
            // Scanner scanner = new Scanner(System.in);
            // System.out.print("确认删除这些文件吗？(输入 'yes' 确认): ");
            // String input = scanner.nextLine().trim();
            //
            // if (!input.equalsIgnoreCase("yes")) {
            //     return "取消：删除操作已取消";
            // }

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
                    failedFiles.add(file.getFileName() + " (" + e.getMessage() + ")");
                }
            }
            StringBuilder result = new StringBuilder();
            result.append("删除完成：成功 ").append(successCount)
                    .append(" 个，失败 ").append(failCount).append(" 个\n");
            if (failCount > 0) {
                result.append("失败的文件：").append(String.join(", ", failedFiles));
            }
            return result.toString();
        } catch (IOException e) {
            return "错误：读取目录时发生IO异常 - " + e.getMessage();
        }
    }
}
