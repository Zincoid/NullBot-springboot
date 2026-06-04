package com.zincoid.nullbot.core.service.file.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.model.data.query.FileQuery;
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
import com.zincoid.nullbot.core.util.DownloadUtil;
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

    @Value("${file.init}")
    private boolean init;

    // @PostConstruct  // 阻塞启动
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!init) return;
        log.info("◎ [FileService] 初始化文件同步中...");
        scanAndSyncFiles();
    }

    // ================= BOT 功能相关 =================

    @Override
    public List<FilePO> search(String key, String fullDir) {
        return searchByFullDir(key, fullDir, false);
    }

    @Override
    public FileInfo upload(String url, String directory, String fileName, Long ownerId, String ownerName) {
        FileInfo fileInfo = DownloadUtil.save(url, directory, fileName);
        boolean recorded = addOrUpdateRecord(directory, fileInfo.getName(),
                fileInfo.getSize(), fileInfo.getLastModified(), ownerId, ownerName);
        if (!recorded) {
            FileUtils.deleteQuietly(new File(fileInfo.getPath()));
            throw new CommonException("数据库记录失败，已清理本地文件");
        }
        return fileInfo;
    }

    @Override
    public boolean delete(String directory, String fileName) {
        FileUtils.deleteQuietly(new File(directory + "/" + fileName));
        return lambdaUpdate()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, fileName)
                .remove();
    }

    // ================= WEB 功能相关 =================

    @Override
    @Transactional
    public boolean initRoot() {
        Path rootPath = Path.of(storageProperties.getFileDirectory());
        String rootParentPath = rootPath.getParent().toString();
        String rootFileName = rootPath.getFileName().toString();
        if (lambdaQuery().eq(FilePO::getDirectory, rootParentPath)
                .eq(FilePO::getFileName, rootFileName).one() != null)
            return false;
        FilePO newRoot = new FilePO(rootFileName, 0L, rootParentPath, 1,
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
    public void syncFiles() {
        scanAndSyncFiles();
    }

    @Override
    public PageResult<FilePO> getPage(FileQuery query) {
        String fullDir = getResolvedFullDir(query.getCurDir());
        return PageResult.of(page(query.toPage(),
                lambdaQuery().eq(FilePO::getDirectory, fullDir)
                        .eq(query.getHidden(), FilePO::getVisible, true)
                        .getWrapper()));
    }

    @Override
    public List<FilePO> search(String key, String curDir, boolean hidden) {
        return searchByFullDir(key, getResolvedFullDir(curDir), hidden);
    }

    @Override
    @Transactional
    public void upload(Long ownerId, MultipartFile uploadFile, String curDir) throws IOException {
        String fileName = uploadFile.getOriginalFilename();
        String fullDir = getResolvedFullDir(curDir);
        FilePO dir = checkDirectoryExists(fullDir);
        checkNameConflict(fullDir, fileName, null);

        String filePath = fullDir + "/" + fileName;
        uploadFile.transferTo(new File(filePath));

        save(new FilePO(uploadFile.getOriginalFilename(), uploadFile.getSize(),
                fullDir, 0, dir.getVisible(), ownerId,
                adminService.getById(ownerId).getUsername(),
                getLastModifiedTime(Path.of(filePath))));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        FilePO file = checkFileExists(id);
        String filePath = file.getDirectory() + "/" + file.getFileName();
        FileUtils.deleteQuietly(new File(filePath));
        if (file.getIsDir() == 1)
            lambdaUpdate()
                    .eq(FilePO::getDirectory, filePath)
                    .or()
                    .likeRight(FilePO::getDirectory, filePath + "/")
                    .remove();
        removeById(id);
    }

    @Override
    @Transactional
    public void download(Integer id, HttpServletRequest request, HttpServletResponse response) {
        FilePO file = checkFileExists(id);
        String fileName = file.getFileName();
        Path filePath = Path.of(file.getDirectory(), fileName);
        String mimeType = request.getSession().getServletContext().getMimeType(fileName);
        if (mimeType == null || mimeType.isEmpty())
            mimeType = "application/octet-stream";
        try (InputStream fileInputStream = Files.newInputStream(filePath);
             ServletOutputStream os = response.getOutputStream()) {
            response.setContentType(mimeType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
            FileCopyUtils.copy(fileInputStream, os);
        } catch (IOException e) {
            throw new RuntimeException("从磁盘下载文件时出错", e);
        }
    }

    @Override
    @Transactional
    public void createDir(Long ownerId, String curDir, String dirName) throws IOException {
        String fullDir = getResolvedFullDir(curDir);
        FilePO dir = checkDirectoryExists(fullDir);
        checkNameConflict(fullDir, dirName, null);
        Path dirPath = Path.of(fullDir, dirName);
        Files.createDirectory(dirPath);
        save(new FilePO(dirName, 0L, fullDir, 1, dir.getVisible(), ownerId,
                adminService.getById(ownerId).getUsername(),
                getLastModifiedTime(dirPath)));
    }

    @Override
    @Transactional
    public void rename(Integer id, String newFileName) {
        FilePO file = checkFileExists(id);
        if (newFileName == null || newFileName.trim().isEmpty())
            throw new CommonException("新文件名不能为空");
        if (newFileName.contains("/") || newFileName.contains("\\") ||
                newFileName.contains(":") || newFileName.contains("*") ||
                newFileName.contains("?") || newFileName.contains("\"") ||
                newFileName.contains("<") || newFileName.contains(">") ||
                newFileName.contains("|"))
            throw new CommonException("新文件名包含非法字符");
        checkNameConflict(file.getDirectory(), newFileName, id);

        String oldFilePath = file.getDirectory() + "/" + file.getFileName();
        String newFilePath = file.getDirectory() + "/" + newFileName;
        if (!new File(oldFilePath).renameTo(new File(newFilePath)))
            throw new RuntimeException("磁盘文件重命名失败");
        if (file.getIsDir() == 1)
            updateSubFilesPath(oldFilePath, newFilePath);
        file.setFileName(newFileName);
        updateById(file);
    }

    @Override
    @Transactional
    public void move(Integer id, String newDir) {
        FilePO sourceFile = checkFileExists(id);
        String targetFullDir = getResolvedFullDir(newDir);
        if (sourceFile.getDirectory().equals(targetFullDir))
            throw new CommonException("数据库路径未修改");
        checkDirectoryExists(targetFullDir);
        checkNameConflict(targetFullDir, sourceFile.getFileName(), null);

        String sourcePath = sourceFile.getDirectory() + "/" + sourceFile.getFileName();
        String targetPath = targetFullDir + "/" + sourceFile.getFileName();
        if (!new File(sourcePath).renameTo(new File(targetPath)))
            throw new RuntimeException("磁盘文件移动失败");
        if (sourceFile.getIsDir() == 1)
            updateSubFilesPath(sourcePath, targetPath);
        sourceFile.setDirectory(targetFullDir);
        updateById(sourceFile);
    }

    @Override
    @Transactional
    public void setVisible(Integer id, boolean visible) {
        FilePO file = checkFileExists(id);
        if (file.getIsDir() == 1) {
            String subDirPath = file.getDirectory() + "/" + file.getFileName();
            lambdaUpdate()
                    .eq(FilePO::getDirectory, subDirPath)
                    .or()
                    .likeRight(FilePO::getDirectory, subDirPath + "/")
                    .set(FilePO::getVisible, visible)
                    .update();
        }
        file.setVisible(visible);
        updateById(file);
    }

    // ================= 记录增改工具 =================

    private boolean addOrUpdateRecord(
            String directory, String fileName, Long fileSize,
            LocalDateTime lastModified, Long ownerId, String ownerName
    ) {
        if (lambdaUpdate()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, fileName)
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
        return save(new FilePO(fileName, fileSize, directory, 0,
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
        if (path == null) return null;
        return path.replace('\\', '/');
    }

    private String getNormalizedBaseDir() {
        return getNormalizedPath(storageProperties.getFileDirectory());
    }

    private String getResolvedFullDir(String curDir) {
        String base = getNormalizedBaseDir();
        if (curDir.equals("/")) return base;
        return base + curDir;
    }

    private static LocalDateTime getLastModifiedTime(Path path) throws IOException {
        return Files.getLastModifiedTime(path)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
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

    private void checkNameConflict(String directory, String fileName, Integer excludeId) {
        long count = lambdaQuery()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, fileName)
                .ne(excludeId != null, FilePO::getId, excludeId)
                .count();
        if (count > 0)
            throw new CommonException("数据库存在同名冲突");
        if (Files.exists(Path.of(directory, fileName)))
            throw new RuntimeException("磁盘存在同名冲突");
    }

    // ================ 通用搜索工具 ================

    private List<FilePO> searchByFullDir(String key, String fullDir, boolean hidden) {
        if (key.contains("/") || key.contains("\\"))
            throw new CommonException("关键字不允许出现斜杠");
        return lambdaQuery()
                .like(FilePO::getFileName, key)
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
        Path rootPath = Path.of(storageProperties.getFileDirectory());
        String rootParentPath = rootPath.getParent().toString();
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
