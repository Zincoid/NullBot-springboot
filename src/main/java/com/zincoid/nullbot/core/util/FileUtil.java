package com.zincoid.nullbot.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Slf4j
@Deprecated
public final class FileUtil {

    private FileUtil() {}

    // =================== 文件列表相关 ===================

    public static String getFileListAsString(String directoryPath, String delimiter, boolean withExtension) {
        Path directory = validateDirectory(directoryPath);
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
                    }).toList();
            if (fileNames.isEmpty()) return "无文件";
            return String.join(delimiter, fileNames);
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    // =================== 路径获取相关 ===================

    public static String getRandomFilePath(String directoryPath) {
        Path directory = validateDirectory(directoryPath);
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            if (files.isEmpty()) return null;
            return files.get(ThreadLocalRandom.current().nextInt(files.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static String getRandomFilePathByPattern(String directoryPath, String pattern) {
        Path directory = validateDirectory(directoryPath);
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> matchedFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .toList();
            if (matchedFiles.isEmpty()) return null;
            return matchedFiles.get(ThreadLocalRandom.current().nextInt(matchedFiles.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static String getRandomFilePathRecursive(String directoryPath) {
        Path startDir = validateDirectory(directoryPath);
        try (Stream<Path> stream = Files.walk(startDir)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            if (files.isEmpty()) return null;
            return files.get(ThreadLocalRandom.current().nextInt(files.size()))
                    .toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static String getFilePathByName(String directoryPath, String fileName) {
        Path directory = validateDirectory(directoryPath);
        try (Stream<Path> stream = Files.list(directory)) {
            Optional<Path> found = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst();
                return found.map(p -> p.toAbsolutePath().toString()).orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static List<String> getFilePathsByPattern(String directoryPath, String pattern) {
        Path directory = validateDirectory(directoryPath);
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .map(path -> path.toAbsolutePath().toString())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static List<String> getFilePathsByKeyword(String directoryPath, String keyword) {
        Path directory = validateDirectory(directoryPath);
        String lowerKeyword = keyword.toLowerCase();
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.contains(lowerKeyword);
                    })
                    .map(path -> path.toAbsolutePath().toString())
                    // .sorted()
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static List<String> getFilePathsRecursive(String directoryPath, String fileName) {
        Path directory = validateDirectory(directoryPath);
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .map(path -> path.toAbsolutePath().toString())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    // =================== 文件删除相关 ===================

    public static void deleteFileByName(String directoryPath, String fileName) {
        Path directory = validateDirectory(directoryPath);
        try {
            Path baseDir = directory.toRealPath();
            Path targetFile = baseDir.resolve(fileName).normalize();
            if (!Files.exists(targetFile)) {
                throw new IllegalArgumentException("文件不存在");
            }
            Path realTarget = targetFile.toRealPath();
            if (!realTarget.startsWith(baseDir)) {
                throw new SecurityException("路径遍历攻击被阻止: " + fileName);
            }
            if (Files.isRegularFile(realTarget)) {
                Files.delete(realTarget);
            } else {
                throw new IllegalArgumentException("不是普通文件: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static int deleteFilesRecursive(String directoryPath, String fileName) {
        Path directory = validateDirectory(directoryPath);
        try {
            Path baseDir = directory.toRealPath();
            List<Path> filesToDelete;
            try (Stream<Path> stream = Files.walk(baseDir)) {
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
                    Path realTarget = file.toRealPath();
                    if (!realTarget.startsWith(baseDir)) {
                        continue;
                    }
                    Files.delete(realTarget);
                    deleteCount++;
                } catch (IOException ignored) {
                }
            }
            return deleteCount;
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    public static List<String> deleteFilesByPattern(String directoryPath, String pattern) {
        Path directory = validateDirectory(directoryPath);
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
            List<String> deletedFiles = new ArrayList<>();
            List<String> rejectedFiles = new ArrayList<>();
            for (Path file : filesToDelete) {
                try {
                    Path targetFile = baseDir.resolve(file.getFileName()).normalize();
                    Path realTarget = targetFile.toRealPath();
                    if (!realTarget.startsWith(baseDir)) {
                        rejectedFiles.add(file.getFileName().toString());
                        continue;
                    }
                    Files.delete(realTarget);
                    deletedFiles.add(file.getFileName().toString());
                } catch (IOException ignored) {
                }
            }
            if (!rejectedFiles.isEmpty()) {
                log.info("安全阻止: {} 个文件删除因路径安全问题被阻止\n{}", rejectedFiles.size(), String.join(", ", rejectedFiles));
            }
            return deletedFiles;
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
    }

    // =================== 内部工具 ===================

    private static Path validateDirectory(String directoryPath) {
        Path directory = Path.of(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + directoryPath);
        }
        return directory;
    }
}
