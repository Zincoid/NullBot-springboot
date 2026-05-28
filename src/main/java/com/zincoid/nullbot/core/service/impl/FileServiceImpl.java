package com.zincoid.nullbot.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.model.data.DataPage;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.web.exception.CommonException;
import com.zincoid.nullbot.core.mapper.AdminMapper;
import com.zincoid.nullbot.core.mapper.FileMapper;
import com.zincoid.nullbot.core.service.FileService;
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
public class FileServiceImpl implements FileService {

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private final AdminMapper adminMapper;
    private final FileMapper fileMapper;
    private final FileStorageProperties fileStorageProperties;

    @Value("${file.init}")
    private boolean init;

    // @PostConstruct  // 阻塞启动
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!init) return;
        log.info("◎ [FileService] 初始化文件同步中...");
        scanAndSyncFiles();
    }

    // =================== BOT功能相关 ===================

    @Override
    public FileInfo saveFile(String url, String directory, String fileName, Long ownerId, String ownerName) {
        FileInfo fileInfo = DownloadUtil.downloadFile(url, directory, fileName);
        boolean recorded = addOrUpdateRecord(directory, fileInfo.getFileName(),
                fileInfo.getFileSize(), fileInfo.getLastModified(), ownerId, ownerName);
        if (!recorded) {
            FileUtils.deleteQuietly(new File(directory + "/" + fileInfo.getFileName()));
            throw new CommonException("数据库记录失败，已清理本地文件");
        }
        return fileInfo;
    }

    @Override
    public boolean deleteFile(String directory, String fileName) {
        FileUtils.deleteQuietly(new File(directory + "/" + fileName));
        return fileMapper.delete(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, fileName)
        ) == 1;
    }

    private boolean addOrUpdateRecord(
            String directory, String fileName, Long fileSize,
            LocalDateTime lastModified, Long ownerId, String ownerName
    ) {
        FilePO existFile = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, directory)
                .eq(FilePO::getFileName, fileName));
        if (existFile != null) {
            existFile.setFileSize(fileSize);
            existFile.setLastModified(lastModified);
            existFile.setOwnerId(ownerId);
            existFile.setOwnerName(ownerName);
            return fileMapper.updateById(existFile) == 1;
        }

        Path path = Path.of(directory);
        FilePO dir = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, path.getParent().toString())
                .eq(FilePO::getFileName, path.getFileName().toString())
                .eq(FilePO::getIsDir, 1)
        );
        if (dir == null) {
            return false;
        }

        FilePO file = new FilePO();
        file.setDirectory(directory);
        file.setFileName(fileName);
        file.setFileSize(fileSize);
        file.setIsDir(0);
        file.setVisible(dir.getVisible());
        file.setLastModified(lastModified);
        file.setOwnerId(ownerId);
        file.setOwnerName(ownerName);

        return fileMapper.insert(file) == 1;
    }

    // =================== WEB功能相关 ===================

    @Override
    @Transactional
    public boolean initRoot() {
        Path rootPath = Path.of(fileStorageProperties.getFileDirectory());
        String rootParentPath = rootPath.getParent().toString();
        String rootFileName = rootPath.getFileName().toString();

        if (fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, rootParentPath)
                .eq(FilePO::getFileName, rootFileName)) != null
        ) {
            return false;
        }

        FilePO newRoot = new FilePO();
        newRoot.setDirectory(rootParentPath);
        newRoot.setFileName(rootFileName);
        newRoot.setFileSize(0L);
        newRoot.setIsDir(1);
        newRoot.setVisible(true);
        newRoot.setLastModified(LocalDateTime.now());
        newRoot.setOwnerId(0L);
        newRoot.setOwnerName("root");

        FilePO existRoot = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getOwnerId, 0L)
                .eq(FilePO::getOwnerName, "root")
        );

        if (existRoot != null) {
            newRoot.setId(existRoot.getId());
            return fileMapper.updateById(newRoot) == 1;
        }
        return fileMapper.insert(newRoot) == 1;
    }

    @Override
    // @Transactional(isolation = Isolation.SERIALIZABLE)
    @Transactional
    public void syncLocalToDatabase() {
        scanAndSyncFiles();
    }

    @Override
    public DataPage<FilePO> getPage(String curDir, Integer current, Integer size, boolean hidden) {
        String fullDir = resolveFullDir(curDir);
        LambdaQueryWrapper<FilePO> wrapper = new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, fullDir)
                .orderByDesc(FilePO::getIsDir)
                .orderByAsc(FilePO::getId);
        if (hidden) wrapper.eq(FilePO::getVisible, true);
        Page<FilePO> page = new Page<>(current, size);
        Page<FilePO> filePage = fileMapper.selectPage(page, wrapper);
        return new DataPage<>(filePage.getRecords(), filePage.getCurrent(), filePage.getPages(), filePage.getTotal(), filePage.getSize());
    }

    @Override
    public List<FilePO> search(String key, String curDir, boolean hidden) {
        if (key.contains("/") || key.contains("\\")) {
            throw new CommonException("关键字不允许出现斜杠");
        }
        String fullDir = resolveFullDir(curDir);
        return hidden ? fileMapper.searchFileVisible(key, fullDir) : fileMapper.searchFile(key, fullDir);
    }

    @Override
    public List<FilePO> search(String key, String fullDir) {
        if (key.contains("/") || key.contains("\\")) {
            throw new CommonException("关键字不允许出现斜杠");
        }
        return fileMapper.searchFile(key, fullDir);
    }

    @Override
    @Transactional
    public void upload(Long ownerId, MultipartFile uploadFile, String curDir) throws IOException {
        String fileName = uploadFile.getOriginalFilename();
        String fullDir = resolveFullDir(curDir);

        if (!fileMapper.selectList(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, fullDir).eq(FilePO::getFileName, fileName)).isEmpty()) {
            throw new CommonException("数据库存在同名冲突");
        }

        Path path = Path.of(fullDir);
        FilePO dir = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, path.getParent().toString())
                .eq(FilePO::getFileName, path.getFileName().toString())
                .eq(FilePO::getIsDir, 1)
        );
        if (dir == null)
            throw new IllegalArgumentException("数据库父目录不存在");
        if (!Files.exists(path))
            throw new IllegalArgumentException("磁盘父目录不存在");

        String filePath = fullDir + "/" + fileName;
        uploadFile.transferTo(new File(filePath));

        String ownerName = adminMapper.selectById(ownerId).getUsername();
        LocalDateTime lastModified = getLastModifiedTime(Path.of(filePath));

        FilePO file = new FilePO();
        file.setFileName(uploadFile.getOriginalFilename());
        file.setFileSize(uploadFile.getSize());
        file.setDirectory(fullDir);
        file.setIsDir(0);
        file.setVisible(dir.getVisible());
        file.setOwnerId(ownerId);
        file.setOwnerName(ownerName);
        file.setLastModified(lastModified);
        fileMapper.insert(file);
    }

    @Override
    @Transactional
    public void download(Integer id, HttpServletRequest request, HttpServletResponse response) {
        FilePO file = fileMapper.selectById(id);
        if (file == null)
            throw new IllegalArgumentException("数据库文件不存在");
        String fileName = file.getFileName();
        Path filePath = Path.of(file.getDirectory(), fileName);
        String mimeType = request.getSession().getServletContext().getMimeType(fileName);
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
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
        String fullDir = resolveFullDir(curDir);

        Path path = Path.of(fullDir);
        FilePO dir = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, path.getParent().toString())
                .eq(FilePO::getFileName, path.getFileName().toString())
                .eq(FilePO::getIsDir, 1)
        );
        if (dir == null)
            throw new IllegalArgumentException("数据库父目录不存在");
        if (!Files.exists(path) || !Files.isDirectory(path))
            throw new IllegalArgumentException("磁盘父目录不存在");

        Path dirPath = Path.of(fullDir, dirName);

        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath);
        } else
            throw new CommonException("磁盘目录已存在");

        String ownerName = adminMapper.selectById(ownerId).getUsername();
        LocalDateTime lastModified = getLastModifiedTime(dirPath);

        FilePO file = new FilePO();
        file.setFileName(dirName);
        file.setFileSize(0L);
        file.setDirectory(fullDir);
        file.setIsDir(1);
        file.setVisible(dir.getVisible());
        file.setOwnerId(ownerId);
        file.setOwnerName(ownerName);
        file.setLastModified(lastModified);
        fileMapper.insert(file);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        FilePO file = fileMapper.selectById(id);
        String filePath = file.getDirectory() + "/" + file.getFileName();
        FileUtils.deleteQuietly(new File(filePath));
        if (file.getIsDir() == 1)
            fileMapper.delete(new LambdaQueryWrapper<FilePO>().likeRight(FilePO::getDirectory, filePath));
        fileMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void rename(Integer id, String newFileName) {
        FilePO file = fileMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("数据库文件不存在");
        }
        if (newFileName == null || newFileName.trim().isEmpty()) {
            throw new CommonException("新文件名不能为空");
        }
        newFileName = newFileName.trim();
        if (newFileName.contains("/") || newFileName.contains("\\") ||
                newFileName.contains(":") || newFileName.contains("*") ||
                newFileName.contains("?") || newFileName.contains("\"") ||
                newFileName.contains("<") || newFileName.contains(">") ||
                newFileName.contains("|")) {
            throw new CommonException("新文件名包含非法字符");
        }

        // 检查是否重名
        LambdaQueryWrapper<FilePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FilePO::getDirectory, file.getDirectory())
                .eq(FilePO::getFileName, newFileName)
                .ne(FilePO::getId, id); // 排除当前文件
        Long count = fileMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new CommonException("数据库目录存在同名文件");
        }

        String oldFilePath = file.getDirectory() + "/" + file.getFileName();
        String newFilePath = file.getDirectory() + "/" + newFileName;
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (!oldFile.exists()) {
            throw new IllegalArgumentException("磁盘原文件不存在");
        }
        if (newFile.exists()) {
            throw new IllegalArgumentException("磁盘新文件已存在");
        }

        // 重命名文件
        boolean renameSuccess = oldFile.renameTo(newFile);
        if (!renameSuccess) {
            throw new RuntimeException("磁盘文件重命名失败");
        }

        // 目录需更新所有子文件的路径
        if (file.getIsDir() == 1) {
            updateSubFilesPath(oldFilePath, newFilePath);
        }

        // 更新数据库
        file.setFileName(newFileName);
        fileMapper.updateById(file);
    }

    @Override
    @Transactional
    public void move(Integer id, String newDir) {
        FilePO sourceFile = fileMapper.selectById(id);
        if (sourceFile == null) {
            throw new IllegalArgumentException("数据库文件不存在");
        }

        String targetFullDir = resolveFullDir(newDir);

        // 检查目录是否未修改
        if (sourceFile.getDirectory().equals(targetFullDir)) {
            throw new CommonException("数据库路径未修改");
        }

        // 检查目标目录存在
        // 数据库检查
        Path targetPath = Path.of(targetFullDir);
        FilePO targetDir = fileMapper.selectOne(new LambdaQueryWrapper<FilePO>()
                .eq(FilePO::getDirectory, targetPath.getParent().toString())
                .eq(FilePO::getFileName, targetPath.getFileName().toString())
                .eq(FilePO::getIsDir, 1));
        if (targetDir == null) {
            throw new CommonException("数据库目标路径不存在");
        }
        // 文件系统检查
        if (!Files.exists(targetPath) || !Files.isDirectory(targetPath)) {
            throw new IllegalArgumentException("磁盘目标路径不存在");
        }

        // 检查目标目录是否存在同名
        LambdaQueryWrapper<FilePO> conflictCheck = new LambdaQueryWrapper<>();
        conflictCheck.eq(FilePO::getDirectory, targetFullDir)
                .eq(FilePO::getFileName, sourceFile.getFileName());

        if (fileMapper.selectCount(conflictCheck) > 0) {
            throw new CommonException("数据库路径下存在同名文件");
        }

        // 检查文件系统是否存在冲突
        String sourcePath = sourceFile.getDirectory() + "/" + sourceFile.getFileName();
        String targetPathStr = targetFullDir + "/" + sourceFile.getFileName();
        File sourceFileSystem = new File(sourcePath);
        File targetFileSystem = new File(targetPathStr);
        if (!sourceFileSystem.exists()) {
            throw new IllegalArgumentException("磁盘源文件不存在");
        }
        if (targetFileSystem.exists()) {
            throw new IllegalArgumentException("磁盘目标路径存在同名文件");
        }

        // 执行文件系统移动操作
        try {
            boolean moveSuccess = sourceFileSystem.renameTo(targetFileSystem);
            if (!moveSuccess) {
                throw new RuntimeException("磁盘文件移动失败");
            }
        } catch (SecurityException e) {
            throw new RuntimeException("磁盘权限不足无法移动");
        }

        // 如果是目录需更新子文件路径
        if (sourceFile.getIsDir() == 1) {
            updateSubFilesPath(sourceFile.getDirectory() + "/" + sourceFile.getFileName(),
                    targetFullDir + "/" + sourceFile.getFileName());
        }

        // 更新数据库记录
        // 保存源文件的 visible 状态 或者 继承目标目录的 visible (根据需求选择)
        // sourceFile.setVisible(targetDir.getVisible());
        sourceFile.setDirectory(targetFullDir);
        fileMapper.updateById(sourceFile);
    }

    @Override
    @Transactional
    public void setVisible(Integer id, boolean visible) {
        FilePO file = fileMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("数据库文件不存在");
        }
        if (file.getIsDir() == 1) {
            List<FilePO> subFiles = fileMapper.selectList(
                    new LambdaQueryWrapper<FilePO>()
                            .likeRight(FilePO::getDirectory, file.getDirectory() + "/" + file.getFileName())
            );
            for (FilePO subFile : subFiles) {
                subFile.setVisible(visible);
                fileMapper.updateById(subFile);
            }
        }
        file.setVisible(visible);
        fileMapper.updateById(file);
    }

    // =================== 其他工具 ===================

    /**
     * 更新子文件的路径
     *
     * @param oldDirPath 原目录路径
     * @param newDirPath 新目录路径
     */
    private void updateSubFilesPath(String oldDirPath, String newDirPath) {
        // 查询原目录路径开头文件
        LambdaQueryWrapper<FilePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(FilePO::getDirectory, oldDirPath);
        List<FilePO> subFiles = fileMapper.selectList(queryWrapper);
        for (FilePO subFile : subFiles) {
            // 替换目录路径部分
            String newSubDirPath = subFile.getDirectory().replace(oldDirPath, newDirPath);
            subFile.setDirectory(newSubDirPath);
            fileMapper.updateById(subFile);
        }
    }

    // =================== 路径 & 时间工具 ===================

    private String normalizePath(String path) {
        if (path == null) return null;
        // 将 Windows 的反斜杠替换为正斜杠
        return path.replace('\\', '/');
    }

    private String getNormalizedBaseDir() {
        return normalizePath(fileStorageProperties.getFileDirectory());
    }

    private String resolveFullDir(String curDir) {
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

    // =================== 本地系统文件与数据库同步工具 ===================

    private record SyncFileInfo(long size, long lastModified, boolean isDirectory) {}

    // 主同步方法 (用户调用)
    public void scanAndSyncFiles() {
        if (!isScanning.compareAndSet(false, true))
            throw new CommonException("已有文件同步任务进行中");
        try {
            // 1. 获取存储目录
            String baseDir = getNormalizedBaseDir();
            Path basePath = Path.of(baseDir);
            if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
                log.info("◎ [FileService] 存储目录不存在: {}", baseDir);
                return;
            }

            // 2. 扫描文件系统
            Map<String, SyncFileInfo> fileSystemMap = new HashMap<>();
            scanDirectory(basePath, fileSystemMap);

            // 3. 获取数据记录
            List<FilePO> dbFiles = fileMapper.selectList(null);
            Map<String, FilePO> dbMap = new HashMap<>();
            for (FilePO file : dbFiles) {
                String normalizedPath = normalizePath(file.getDirectory() + "/" + file.getFileName());
                file.setDirectory(normalizePath(file.getDirectory()));
                dbMap.put(normalizedPath, file);
            }

            // 4. 开始同步处理
            syncFiles(fileSystemMap, dbMap);

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
                String normalizedPath = normalizePath(child.toAbsolutePath().toString());
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

    // 同步文件系统与数据库
    private void syncFiles(Map<String, SyncFileInfo> fileSystemMap, Map<String, FilePO> dbMap) {
        for (var entry : fileSystemMap.entrySet()) {
            String path = entry.getKey();
            SyncFileInfo info = entry.getValue();
            Path filePath = Path.of(path);

            if (dbMap.containsKey(path)) {
                // 检查文件是否被修改 (性能损耗大)
                FilePO dbFile = dbMap.get(path);
                if (dbFile.getFileSize() != info.size() ||
                        dbFile.getLastModified() == null ||
                        dbFile.getLastModified().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() != info.lastModified()) {
                    // 更新文件信息
                    dbFile.setFileSize(info.size());
                    dbFile.setLastModified(Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    fileMapper.updateById(dbFile);
                }
            } else {
                // 新增文件记录
                FilePO newFile = new FilePO();
                newFile.setFileName(filePath.getFileName().toString());
                newFile.setFileSize(info.size());
                newFile.setDirectory(normalizePath(filePath.getParent().toString()));
                newFile.setIsDir(info.isDirectory() ? 1 : 0);
                newFile.setLastModified(Instant.ofEpochMilli(info.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                fileMapper.insert(newFile);
            }
        }
        // 处理已删除的文件
        Path rootPath = Path.of(fileStorageProperties.getFileDirectory());
        String rootParentPath = rootPath.getParent().toString();
        String rootFileName = rootPath.getFileName().toString();

        for (Map.Entry<String, FilePO> entry : dbMap.entrySet()) {
            if (entry.getValue().getDirectory().equals(rootParentPath) && entry.getValue().getFileName().equals(rootFileName)) {
                continue;  // 跳过根文件
            }
            String path = entry.getKey();
            if (!fileSystemMap.containsKey(path)) {
                // 数据库中有但文件系统中已删除
                fileMapper.delete(new LambdaQueryWrapper<FilePO>().apply("CONCAT(directory, '/', file_name) = {0}", path));
            }
        }
    }
}
