package com.zincoid.nullbot.core.service.file.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.model.data.query.FileQuery;
import com.zincoid.nullbot.core.service.basic.UserService;
import com.zincoid.nullbot.core.service.system.AdminService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.web.exception.CommonException;
import com.zincoid.nullbot.core.mapper.FileMapper;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.DownloadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePO> implements FileService {

    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final StorageProperties storageProperties;
    private final AdminService adminService;
    private final UserService userService;

    @Value("${file.init}")
    private boolean init;

    // ================== 预载方法 ==================

    // @PostConstruct  // 阻塞启动
    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        if (!init) return;
        log.info("◎ [FileService] 初始化文件同步中...");
        scanAndSyncFiles();
    }

    // ================== 应用方法 ==================

    @Override
    @Transactional
    public boolean init() {
        Path root = Path.of(getNormalizedBaseDir());
        Path rootParent = root.getParent();
        if (rootParent == null)
            throw new CommonException("根目录不能为系统根");
        String rootDir = getNormalizedPath(rootParent.toString());
        String rootName = root.getFileName().toString();
        if (lambdaQuery().eq(FilePO::getDirectory, rootDir)
                .eq(FilePO::getFileName, rootName).one() != null)
            return false;
        FilePO newRoot = new FilePO(rootName, 0L, rootDir, 1,
                true, 0L, "root", LocalDateTime.now());
        FilePO existRoot = lambdaQuery()
                .eq(FilePO::getOwnerId, 0L)
                .eq(FilePO::getOwnerName, "root")
                .one();
        if (existRoot != null) {
            newRoot.setId(existRoot.getId());
            return updateById(newRoot);
        }
        return save(newRoot);
    }

    @Override
    @Transactional
    public void sync() {
        scanAndSyncFiles();
    }

    @Override
    public PageResult<FilePO> page(FileQuery query) {
        String directory = getResolvedDirectory(query.getDirectory());
        return PageResult.of(page(query.toPage(),
                lambdaQuery().eq(FilePO::getDirectory, directory)
                        .eq(query.getHidden(), FilePO::getVisible, true)
                        .getWrapper()));
    }

    @Override
    public List<FilePO> list(String directory) {
        return searchByFullDir(null, getResolvedDirectory(directory), false);
    }

    @Override
    public List<FilePO> search(String keyword, String directory) {
        return searchByFullDir(keyword, getResolvedDirectory(directory), false);
    }

    @Override
    public List<FilePO> search(String keyword, String directory, boolean hidden) {
        return searchByFullDir(keyword, getResolvedDirectory(directory), hidden);
    }

    @Override
    public FileInfo upload(String url, String directory, String filename, Long uid) {
        directory = getResolvedDirectory(directory);
        FileInfo fileInfo = DownloadUtil.save(url, directory, filename);
        boolean recorded = addOrUpdateRecord(directory, fileInfo.getName(),
                fileInfo.getSize(), fileInfo.getLastModified(),
                uid, userService.getById(uid).getName());
        if (!recorded) {
            FileUtils.deleteQuietly(new File(fileInfo.getPath()));
            throw new RuntimeException("数据更新失败");
        }
        return fileInfo;
    }

    @Override
    @Transactional
    public void upload(MultipartFile file, String directory, Long uid) {
        directory = getResolvedDirectory(directory);
        String filename = file.getOriginalFilename();
        FilePO dir = checkDirectoryExists(directory);
        checkNameConflict(directory, filename, null);
        String filePath = directory + "/" + filename;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        try {
            save(new FilePO(file.getOriginalFilename(), file.getSize(),
                    directory, 0, dir.getVisible(), uid,
                    adminService.getById(uid).getUsername(),
                    getLastModifiedTime(Path.of(filePath))));
        } catch (Exception e) {
            FileUtils.deleteQuietly(new File(filePath));
            throw new RuntimeException("数据更新失败", e);
        }
    }

    @Override
    @Transactional
    public void delete(String directory, String filename) {
        directory = getResolvedDirectory(directory);
        FilePO file = checkFileExists(directory, filename);
        delete(file.getId());
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        FilePO file = checkFileExists(id);
        String filePath = file.getDirectory() + "/" + file.getFileName();
        if (file.getIsDir() == 1)
            lambdaUpdate()
                    .eq(FilePO::getDirectory, filePath)
                    .or()
                    .likeRight(FilePO::getDirectory, filePath + "/")
                    .remove();
        removeById(id);
        FileUtils.deleteQuietly(new File(filePath));
    }

    @Override
    @Transactional
    public void download(Integer id, HttpServletRequest req, HttpServletResponse res) {
        FilePO file = checkFileExists(id);
        String filename = file.getFileName();
        Path filePath = Path.of(file.getDirectory(), filename);
        String mimeType = req.getSession().getServletContext().getMimeType(filename);
        if (mimeType == null || mimeType.isEmpty())
            mimeType = "application/octet-stream";
        try (InputStream fileInputStream = Files.newInputStream(filePath);
             ServletOutputStream os = res.getOutputStream()) {
            res.setContentType(mimeType);
            res.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");
            FileCopyUtils.copy(fileInputStream, os);
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    @Transactional
    public void mkdir(String directory, String name, Long uid) {
        directory = getResolvedDirectory(directory);
        FilePO dir = checkDirectoryExists(directory);
        checkNameConflict(directory, name, null);
        Path dirPath = Path.of(directory, name);
        try {
            Files.createDirectory(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("目录创建失败", e);
        }
        try {
            save(new FilePO(name, 0L, directory, 1, dir.getVisible(), uid,
                    adminService.getById(uid).getUsername(),
                    getLastModifiedTime(dirPath)));
        } catch (Exception e) {
            FileUtils.deleteQuietly(dirPath.toFile());
            throw new RuntimeException("数据更新失败", e);
        }
    }

    @Override
    @Transactional
    public void rename(Integer id, String filename) {
        FilePO file = checkFileExists(id);
        if (filename == null || filename.trim().isEmpty())
            throw new CommonException("新文件名不能为空");
        if (filename.contains("/") || filename.contains("\\") ||
                filename.contains(":") || filename.contains("*") ||
                filename.contains("?") || filename.contains("\"") ||
                filename.contains("<") || filename.contains(">") ||
                filename.contains("|"))
            throw new CommonException("新文件名包含非法字符");
        checkNameConflict(file.getDirectory(), filename, id);

        String oldFilePath = file.getDirectory() + "/" + file.getFileName();
        String newFilePath = file.getDirectory() + "/" + filename;
        if (file.getIsDir() == 1)
            updateSubFilesPath(oldFilePath, newFilePath);
        file.setFileName(filename);
        updateById(file);
        if (!new File(oldFilePath).renameTo(new File(newFilePath)))
            throw new RuntimeException("磁盘文件更名失败");
    }

    @Override
    @Transactional
    public void move(Integer id, String directory) {
        directory = getResolvedDirectory(directory);
        FilePO file = checkFileExists(id);
        if (file.getDirectory().equals(directory))
            throw new CommonException("数据库路径未修改");
        checkDirectoryExists(directory);
        checkNameConflict(directory, file.getFileName(), null);

        String sourcePath = file.getDirectory() + "/" + file.getFileName();
        if (file.getIsDir() == 1 && directory.startsWith(sourcePath + "/"))
            throw new CommonException("无法将目录移入自身子目录");
        String targetPath = directory + "/" + file.getFileName();
        if (file.getIsDir() == 1)
            updateSubFilesPath(sourcePath, targetPath);
        file.setDirectory(directory);
        updateById(file);
        if (!new File(sourcePath).renameTo(new File(targetPath)))
            throw new RuntimeException("磁盘文件移动失败");
    }

    @Override
    @Transactional
    public void visualize(Integer id, boolean flag) {
        FilePO file = checkFileExists(id);
        if (file.getIsDir() == 1) {
            String subDirPath = file.getDirectory() + "/" + file.getFileName();
            lambdaUpdate()
                    .eq(FilePO::getDirectory, subDirPath)
                    .or()
                    .likeRight(FilePO::getDirectory, subDirPath + "/")
                    .set(FilePO::getVisible, flag)
                    .update();
        }
        file.setVisible(flag);
        updateById(file);
    }

    // ================= 记录增改工具 =================

    private boolean addOrUpdateRecord(
            String directory, String filename, Long fileSize,
            LocalDateTime lastModified, Long ownerId, String ownerName
    ) {
        if (lambdaUpdate()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, filename)
                .set(FilePO::getFileSize, fileSize)
                .set(FilePO::getLastModified, lastModified)
                .set(FilePO::getOwnerId, ownerId)
                .set(FilePO::getOwnerName, ownerName)
                .update())
            return true;
        Path path = Path.of(directory);
        FilePO dir = lambdaQuery()
                .eq(FilePO::getDirectory, path.getParent().toString())
                .eq(FilePO::getFileName, path.getFileName().toString())
                .eq(FilePO::getIsDir, 1)
                .one();
        if (dir == null) return false;
        return save(new FilePO(filename, fileSize, directory, 0,
                dir.getVisible(), ownerId, ownerName, lastModified));
    }

    // ================= 路径更新工具 =================

    private void updateSubFilesPath(String oldDirPath, String newDirPath) {
        // 直接匹配目录批量更新
        lambdaUpdate()
                .eq(FilePO::getDirectory, oldDirPath)
                .set(FilePO::getDirectory, newDirPath)
                .update();
        // 子目录需逐条替换路径
        List<FilePO> subFiles = lambdaQuery()
                .likeRight(FilePO::getDirectory, oldDirPath + "/")
                .list();
        for (FilePO subFile : subFiles) {
            subFile.setDirectory(subFile.getDirectory().replace(oldDirPath, newDirPath));
            updateById(subFile);
        }
    }

    // ================ 路径时间工具 ================

    private String getNormalizedPath(String path) {
        if (path == null) throw new IllegalArgumentException("空路径");
        return path.replace('\\', '/');
    }

    private String getNormalizedBaseDir() {
        return getNormalizedPath(storageProperties.getFileDirectory());
    }

    private String getResolvedDirectory(String directory) {
        if (directory == null) throw new IllegalArgumentException("空路径");
        if (!directory.startsWith("/")) directory = "/" + directory;
        String base = getNormalizedBaseDir();
        String normalized = getNormalizedPath(directory);
        if (normalized.startsWith(base)) return normalized;
        if (normalized.equals("/")) return base;
        return base + normalized;
    }

    private static LocalDateTime getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (IOException e) {
            throw new RuntimeException("获取修改时间失败");
        }
    }

    // ================ 通用校验工具 ================

    private FilePO checkFileExists(Integer id) {
        FilePO file = getById(id);
        if (file == null)
            throw new CommonException("数据库文件不存在");
        if (!Files.exists(Path.of(file.getDirectory(), file.getFileName())))
            throw new RuntimeException("磁盘文件不存在");
        return file;
    }

    private FilePO checkFileExists(String fullDir, String filename) {
        FilePO file = lambdaQuery()
                .eq(FilePO::getDirectory, fullDir)
                .eq(FilePO::getFileName, filename)
                .one();
        if (file == null)
            throw new CommonException("数据库文件不存在");
        if (!Files.exists(Path.of(fullDir, filename)))
            throw new RuntimeException("磁盘文件不存在");
        return file;
    }

    private FilePO checkDirectoryExists(String fullDir) {
        Path path = Path.of(fullDir);
        FilePO dir = lambdaQuery()
                .eq(FilePO::getDirectory, path.getParent().toString())
                .eq(FilePO::getFileName, path.getFileName().toString())
                .eq(FilePO::getIsDir, 1)
                .one();
        if (dir == null)
            throw new CommonException("数据库目录不存在");
        if (!Files.exists(path) || !Files.isDirectory(path))
            throw new RuntimeException("磁盘目录不存在");
        return dir;
    }

    private void checkNameConflict(String fullDir, String filename, Integer excludeId) {
        long count = lambdaQuery()
                .eq(FilePO::getDirectory, fullDir)
                .eq(FilePO::getFileName, filename)
                .ne(excludeId != null, FilePO::getId, excludeId)
                .count();
        if (count > 0)
            throw new CommonException("数据库存在同名冲突");
        if (Files.exists(Path.of(fullDir, filename)))
            throw new RuntimeException("磁盘存在同名冲突");
    }

    // ================ 通用搜索工具 ================

    private List<FilePO> searchByFullDir(String keyword, String fullDir, boolean hidden) {
        if (keyword != null && (keyword.contains("/") || keyword.contains("\\")))
            throw new CommonException("关键字不允许出现斜杠");
        return lambdaQuery()
                .like(keyword != null, FilePO::getFileName, keyword)
                .and(w -> w.eq(FilePO::getDirectory, fullDir)
                        .or()
                        .likeRight(FilePO::getDirectory, fullDir + "/"))
                .eq(hidden, FilePO::getVisible, true)
                .list();
    }

    // ================ 文件同步工具 ================

    // 应用同步方法
    public void scanAndSyncFiles() {
        if (!isScanning.compareAndSet(false, true))
            throw new CommonException("已有文件同步任务进行中");
        try {
            // 1. 获取存储目录
            String baseDir = getNormalizedBaseDir();
            Path basePath = Path.of(baseDir);
            if (!Files.exists(basePath) || !Files.isDirectory(basePath))
                throw new RuntimeException("存储目录不存在");
            // 2. 扫描文件系统
            Map<String, SyncFileInfo> fileSystemMap = new HashMap<>();
            scanDirectory(basePath, fileSystemMap);
            // 3. 获取数据记录
            List<FilePO> dbFiles = list();
            Map<String, FilePO> dbMap = new HashMap<>();
            for (FilePO file : dbFiles) {
                String normalizedPath = getNormalizedPath(file.getDirectory() + "/" + file.getFileName());
                file.setDirectory(getNormalizedPath(file.getDirectory()));
                dbMap.put(normalizedPath, file);
            }
            // 4. 开始同步处理
            syncLocalToDb(fileSystemMap, dbMap);
            log.info("◎ [FileService] 文件同步完成 - 共处理文件: {}", fileSystemMap.size());
        } catch (Exception e) {
            log.info("◎ [FileService] 文件同步失败 - {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            isScanning.set(false);
        }
    }

    // 扫描本地文件
    private void scanDirectory(Path dir, Map<String, SyncFileInfo> resultMap) throws IOException {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            for (Path child : stream.toList()) {
                String normalizedPath = getNormalizedPath(child.toAbsolutePath().toString());
                boolean isDir = Files.isDirectory(child);
                resultMap.put(normalizedPath, new SyncFileInfo(
                        Files.size(child),
                        Files.getLastModifiedTime(child).toMillis(),
                        isDir
                ));
                if (isDir) {
                    scanDirectory(child, resultMap);
                }
            }
        }
    }

    // 同步至数据库
    private void syncLocalToDb(Map<String, SyncFileInfo> fileSystemMap, Map<String, FilePO> dbMap) {
        for (var entry : fileSystemMap.entrySet()) {
            String path = entry.getKey();
            SyncFileInfo info = entry.getValue();
            Path filePath = Path.of(path);
            FilePO dbFile = dbMap.get(path);
            if (dbFile != null) {
                // 更新文件信息
                if (dbFile.getFileSize() != info.size() || dbFile.getLastModified() == null ||
                        dbFile.getLastModified().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() != info.lastModified()) {
                    dbFile.setFileSize(info.size());
                    dbFile.setLastModified(Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    updateById(dbFile);
                }
            } else {
                // 新增文件记录
                save(new FilePO(filePath.getFileName().toString(),
                        info.size(), getNormalizedPath(filePath.getParent().toString()),
                        info.isDirectory() ? 1 : 0, null, null, null,
                        Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
            }
        }
        // 处理非法文件
        Path rootPath = Path.of(getNormalizedBaseDir());
        String rootParentPath = getNormalizedPath(rootPath.getParent().toString());
        String rootFileName = rootPath.getFileName().toString();
        for (Map.Entry<String, FilePO> entry : dbMap.entrySet()) {
            // 跳过根文件
            if (entry.getValue().getDirectory().equals(rootParentPath) && entry.getValue().getFileName().equals(rootFileName))
                continue;
            // 清除空文件
            if (!fileSystemMap.containsKey(entry.getKey()))
                removeById(entry.getValue().getId());
        }
    }

    private record SyncFileInfo(long size, long lastModified, boolean isDirectory) {}
}
