package com.zincoid.nullbot.core.service.file.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.model.data.query.FileQuery;
import com.zincoid.nullbot.core.service.base.UserService;
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
import com.zincoid.nullbot.core.model.information.FileMeta;
import com.zincoid.nullbot.web.exception.CommonException;
import com.zincoid.nullbot.core.mapper.FileMapper;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.SaveUtil;
import com.zincoid.nullbot.core.utils.PathUtil;
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

    private record SyncFileInfo(long size, long lastModified, boolean isDirectory) {}

    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final StorageProperties storageProperties;
    private final AdminService adminService;
    private final UserService userService;

    // ================== 预载方法 ==================

    // @PostConstruct  // 阻塞启动
    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        if (!storageProperties.isInit()) return;
        log.info("◎ [FileService] 初始化文件同步中...");
        scanAndSyncFiles();
    }

    // ================== 应用方法 ==================

    @Override
    @Transactional
    public void sync() {
        scanAndSyncFiles();
    }

    @Override
    public PageResult<FilePO> page(FileQuery query) {
        return PageResult.of(page(
                query.toPage(),
                lambdaQuery().eq(FilePO::getDirectory, query.getDirectory())
                        .eq(query.getHidden(), FilePO::getVisible, true)
                        .getWrapper()
        ));
    }

    @Override
    public List<FilePO> list(String directory) {
        return lambdaQuery()
                .eq(FilePO::getDirectory, directory)
                .list();
    }

    @Override
    public List<FilePO> search(String keyword, String directory) {
        return search(keyword, directory, false);
    }

    @Override
    public List<FilePO> search(String keyword, String directory, boolean hidden) {
        if (keyword != null && (keyword.contains("/") || keyword.contains("\\")))
            throw new CommonException("关键字不允许出现斜杠");
        String prefix = directory.equals("/") ? "/" : directory + "/";
        return lambdaQuery()
                .like(keyword != null, FilePO::getFileName, keyword)
                .and(w -> w.eq(FilePO::getDirectory, directory)
                        .or()
                        .likeRight(FilePO::getDirectory, prefix))
                .eq(hidden, FilePO::getVisible, true)
                .list();
    }

    @Override
    public FileMeta upload(String url, String directory, String filename, Long uid) {
        String absoluteDir = toAbsolutePath(directory);
        FileMeta fileMeta = SaveUtil.save(url, absoluteDir, filename);
        boolean recorded = addOrUpdateRecord(directory, fileMeta.getName(),
                fileMeta.getSize(), fileMeta.getLastModified(),
                uid, userService.getById(uid).getName());
        if (!recorded) {
            FileUtils.deleteQuietly(new File(fileMeta.getPath()));
            throw new RuntimeException("数据更新失败");
        }
        return fileMeta;
    }

    @Override
    @Transactional
    public void upload(MultipartFile file, String directory, Long uid) {
        String absoluteDir = toAbsolutePath(directory);
        String filename = file.getOriginalFilename();
        FilePO dir = checkDirectoryExists(directory);
        checkNameConflict(directory, filename, null);
        String filePath = absoluteDir + "/" + filename;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        try {
            save(new FilePO(file.getOriginalFilename(), file.getSize(),
                    directory, false, dir.getVisible(), uid,
                    adminService.getById(uid).getUsername(),
                    getLastModified(Path.of(filePath))));
        } catch (Exception e) {
            FileUtils.deleteQuietly(new File(filePath));
            throw new RuntimeException("数据更新失败", e);
        }
    }

    @Override
    @Transactional
    public void delete(String directory, String filename) {
        FilePO file = checkFileExists(directory, filename);
        delete(file.getId());
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        FilePO file = checkFileExists(id);
        String relativePath = file.getPath();
        String absolutePath = toAbsolutePath(relativePath);
        if (file.getIsDir())
            lambdaUpdate()
                    .eq(FilePO::getDirectory, relativePath)
                    .or()
                    .likeRight(FilePO::getDirectory, relativePath + "/")
                    .remove();
        removeById(id);
        FileUtils.deleteQuietly(new File(absolutePath));
    }

    @Override
    @Transactional
    public void download(Integer id, HttpServletRequest req, HttpServletResponse res) {
        FilePO file = checkFileExists(id);
        String filename = file.getFileName();
        Path filePath = Path.of(toAbsolutePath(file.getDirectory()), filename);
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
        String absoluteDir = toAbsolutePath(directory);
        FilePO dir = checkDirectoryExists(directory);
        checkNameConflict(directory, name, null);
        Path dirPath = Path.of(absoluteDir, name);
        try {
            Files.createDirectory(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("目录创建失败", e);
        }
        try {
            save(new FilePO(name, 0L, directory, true, dir.getVisible(), uid,
                    adminService.getById(uid).getUsername(),
                    getLastModified(dirPath)));
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

        String oldRelativePath = file.getPath();
        String newRelativePath = PathUtil.join(file.getDirectory(), filename);
        String oldAbsolutePath = toAbsolutePath(oldRelativePath);
        String newAbsolutePath = toAbsolutePath(newRelativePath);
        if (file.getIsDir())
            updateSubFilesPath(oldRelativePath, newRelativePath);
        file.setFileName(filename);
        updateById(file);
        if (!new File(oldAbsolutePath).renameTo(new File(newAbsolutePath)))
            throw new RuntimeException("磁盘文件更名失败");
    }

    @Override
    @Transactional
    public void move(Integer id, String directory) {
        FilePO file = checkFileExists(id);
        String oldDir = file.getDirectory();
        if (oldDir.equals(directory))
            throw new CommonException("数据库路径未修改");
        checkDirectoryExists(directory);
        checkNameConflict(directory, file.getFileName(), null);

        String sourceRelative = file.getPath();
        String sourceAbsolute = toAbsolutePath(sourceRelative);
        if (file.getIsDir() && directory.startsWith(sourceRelative + "/"))
            throw new CommonException("无法将目录移入自身子目录");
        String targetRelative = PathUtil.join(directory, file.getFileName());
        String targetAbsolute = toAbsolutePath(targetRelative);
        if (file.getIsDir())
            updateSubFilesPath(sourceRelative, targetRelative);
        file.setDirectory(directory);
        updateById(file);
        if (!new File(sourceAbsolute).renameTo(new File(targetAbsolute)))
            throw new RuntimeException("磁盘文件移动失败");
    }

    @Override
    @Transactional
    public void visualize(Integer id, boolean flag) {
        FilePO file = checkFileExists(id);
        if (file.getIsDir()) {
            String subDirPath = file.getPath();
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
        FilePO dir;
        try {
            dir = checkDirectoryExists(directory);
        } catch (Exception e) {
            return false;
        }
        return save(new FilePO(filename, fileSize, directory, false,
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

    private String toAbsolutePath(String relativePath) {
        return storageProperties.resolve(relativePath);
    }

    private String toRelativePath(String absolutePath) {
        String normalized = getNormalizedPath(absolutePath);
        String base = getNormalizedBase();
        if (normalized.startsWith(base)) {
            String relative = normalized.substring(base.length());
            return relative.isEmpty() ? "/" : relative;
        }
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        return normalized;
    }

    private String getNormalizedPath(String path) {
        return path.replace('\\', '/');
    }

    private String getNormalizedBase() {
        return getNormalizedPath(storageProperties.getFileDirectory());
    }

    private static LocalDateTime getLastModified(Path path) {
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
        if (!Files.exists(Path.of(toAbsolutePath(file.getPath()))))
            throw new RuntimeException("磁盘文件不存在");
        return file;
    }

    private FilePO checkFileExists(String directory, String filename) {
        FilePO file = lambdaQuery()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, filename)
                .one();
        if (file == null)
            throw new CommonException("数据库文件不存在");
        if (!Files.exists(Path.of(toAbsolutePath(directory), filename)))
            throw new RuntimeException("磁盘文件不存在");
        return file;
    }

    private FilePO checkDirectoryExists(String directory) {
        if (directory.equals("/")) {
            Path basePath = Path.of(getNormalizedBase());
            if (!Files.exists(basePath) || !Files.isDirectory(basePath))
                throw new RuntimeException("磁盘目录不存在");
            return FilePO.ofRoot();
        }
        String parentDir = PathUtil.parentOf(directory);
        String dirName = PathUtil.nameOf(directory);
        FilePO dir = lambdaQuery()
                .eq(FilePO::getDirectory, parentDir)
                .eq(FilePO::getFileName, dirName)
                .eq(FilePO::getIsDir, true)
                .one();
        if (dir == null)
            throw new CommonException("数据库目录不存在");
        Path path = Path.of(toAbsolutePath(directory));
        if (!Files.exists(path) || !Files.isDirectory(path))
            throw new RuntimeException("磁盘目录不存在");
        return dir;
    }

    private void checkNameConflict(String directory, String filename, Integer excludeId) {
        long count = lambdaQuery()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, filename)
                .ne(excludeId != null, FilePO::getId, excludeId)
                .count();
        if (count > 0)
            throw new CommonException("数据库存在同名冲突");
        if (Files.exists(Path.of(toAbsolutePath(directory), filename)))
            throw new RuntimeException("磁盘存在同名冲突");
    }

    // ================ 文件同步工具 ================

    public void scanAndSyncFiles() {
        if (!isScanning.compareAndSet(false, true))
            throw new CommonException("已有文件同步任务进行中");
        try {
            // 1. 存储目录获取
            String baseDir = getNormalizedBase();
            Path basePath = Path.of(baseDir);
            if (!Files.exists(basePath) || !Files.isDirectory(basePath))
                throw new RuntimeException("存储目录不存在");
            // 2. 文件系统扫描
            Map<String, SyncFileInfo> fileSystemMap = new HashMap<>();
            scanLocal(basePath, fileSystemMap);
            // 3. 数据记录获取
            Map<String, FilePO> dbMap = scanDb();
            // 4. 同步处理开始
            syncLocalToDb(fileSystemMap, dbMap);
            log.info("◎ [FileService] 文件同步完成 - 共处理文件: {}", fileSystemMap.size());
        } catch (Exception e) {
            log.info("◎ [FileService] 文件同步失败 - {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            isScanning.set(false);
        }
    }

    private Map<String, FilePO> scanDb() {
        Map<String, FilePO> dbMap = new HashMap<>();
        for (FilePO file : list()) {
            // 绝对路径迁移使用
            // file.setDirectory(toRelativePath(file.getDirectory()));
            dbMap.put(file.getPath(), file);
        }
        return dbMap;
    }

    private void scanLocal(Path dir, Map<String, SyncFileInfo> resultMap) throws IOException {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            for (Path child : stream.toList()) {
                String absolutePath = getNormalizedPath(child.toAbsolutePath().toString());
                String relativePath = toRelativePath(absolutePath);
                boolean isDir = Files.isDirectory(child);
                resultMap.put(relativePath, new SyncFileInfo(
                        Files.size(child),
                        Files.getLastModifiedTime(child).toMillis(),
                        isDir
                ));
                if (isDir) {
                    scanLocal(child, resultMap);
                }
            }
        }
    }

    private void syncLocalToDb(Map<String, SyncFileInfo> fileSystemMap, Map<String, FilePO> dbMap) {
        for (var entry : fileSystemMap.entrySet()) {
            String relativePath = entry.getKey();
            Path absolutePath = Path.of(toAbsolutePath(relativePath));
            SyncFileInfo info = entry.getValue();
            FilePO dbFile = dbMap.get(relativePath);
            if (dbFile != null) {
                // 更新文件记录
                if (dbFile.getFileSize() != info.size() || dbFile.getLastModified() == null ||
                        dbFile.getLastModified().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() != info.lastModified()) {
                    dbFile.setFileSize(info.size());
                    dbFile.setLastModified(Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    updateById(dbFile);
                }
                // 绝对路径迁移
                // updateById(dbFile);
            } else {
                // 新增文件记录
                save(new FilePO(absolutePath.getFileName().toString(),
                        info.size(), toRelativePath(absolutePath.getParent().toString()),
                        info.isDirectory(), null, null, null,
                        Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
            }
        }
        for (Map.Entry<String, FilePO> entry : dbMap.entrySet()) {
            if (!fileSystemMap.containsKey(entry.getKey())) {
                // 清除无效记录
                removeById(entry.getValue().getId());
            }
        }
    }
}
