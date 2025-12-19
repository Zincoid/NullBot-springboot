package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.mapper.FileMapper;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService
{
    private final FileMapper fileMapper;
    private final FileStorageConfig fileStorageConfig;

    @Override
    public FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir) {
        scanAndSyncFiles();
        String fullDir;
        if(curDir.equals("/")){
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/");
        }else{
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/") + curDir;
        }
        Page<FilePO> page = new Page<>(currentPage, pageSize);
        Page<FilePO> filePage = fileMapper.selectPage(page, new LambdaQueryWrapper<FilePO>().eq(FilePO::getDirectory, fullDir));
        return new FilePage(filePage.getRecords(), filePage.getCurrent(), filePage.getPages(), filePage.getSize());
    }

    @Override
    public WebResult upload(MultipartFile uploadFile, String curDir) {
        String fileName = uploadFile.getOriginalFilename();
        String fullDir;
        if(curDir.equals("/")){
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/");
        }else{
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/") + curDir;
        }

        FilePO file = new FilePO();
        file.setFileName(uploadFile.getOriginalFilename());
        file.setFileSize(uploadFile.getSize());
        file.setDirectory(fullDir);
        file.setIsDir(0);

        java.io.File file_dir = new java.io.File(fullDir);
        if (!file_dir.exists()) {
            return WebResult.fail().addMsg("目录不存在");
        }

        try {
            uploadFile.transferTo(new java.io.File(fullDir + "/" + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileMapper.insert(file);
        return WebResult.success().addMsg("上传成功");
    }

    @Override
    public WebResult download(Integer id, HttpServletRequest request, HttpServletResponse response) {
        FilePO file = fileMapper.selectById(id);
        if (file == null) {
            return WebResult.fail().addMsg("文件不存在");
        }
        String fileName = file.getFileName();
        String suf = file.getFileName().substring(file.getFileName().lastIndexOf("."));
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new java.io.File(file.getDirectory() + "/" + fileName));
            response.setContentType(request.getSession().getServletContext().getMimeType(suf));//获取文件的mimetype
            response.setHeader("content-disposition","attachment;fileName="+ URLEncoder.encode(fileName,"UTF-8"));
            ServletOutputStream os = response.getOutputStream();
            FileCopyUtils.copy(fileInputStream,os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return WebResult.success().addMsg("下载成功");
    }

    @Override
    public WebResult createDir(String curDir, String dirName) {
        String fullDir;
        if(curDir.equals("/")){
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/");
        }else{
            fullDir = fileStorageConfig.getFileDirectory().replace("\\", "/") + curDir;
        }

        FilePO file = new FilePO();
        file.setFileName(dirName);
        file.setFileSize(0L);
        file.setDirectory(fullDir);
        file.setIsDir(1);

        java.io.File file_dir = new java.io.File(fullDir);
        if(!file_dir.exists() || file_dir.isFile())
            return WebResult.fail().addMsg("curDir不合法");

        java.io.File new_dir = new java.io.File(fullDir + dirName);
        if (!new_dir.exists()) {
            new_dir.mkdir();
        } else {
            return WebResult.fail().addMsg("目录已存在");
        }

        fileMapper.insert(file);
        return WebResult.success().addMsg("创建成功");
    }

    @Override
    public WebResult deleteFile(Integer id) {
        FilePO file = fileMapper.selectById(id);
        deleteFileByDir(new java.io.File(file.getDirectory() + "/" + file.getFileName()));
        fileMapper.deleteById(id);
        return WebResult.success().addMsg("删除成功");
    }

    public static void deleteFileByDir(java.io.File dir){
        java.io.File[] files = dir.listFiles();
        if(files != null && files.length > 0){
            for(java.io.File f : files){
                if(f.isFile()){
                    f.delete();
                } else {
                    deleteFileByDir(f);
                }
            }
        }
        dir.delete();
    }

    // 系统同步方法
    public void scanAndSyncFiles() {
        try {
            // 1. 获取存储目录
            String baseDir = normalizePath(fileStorageConfig.getFileDirectory());
            File baseDirectory = new File(baseDir);

            if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
                System.err.println("存储目录不存在: " + baseDir);
                return;
            }

            // 2. 扫描文件系统
            Map<String, FileInfo> fileSystemMap = new HashMap<>();
            scanDirectory(baseDirectory, fileSystemMap);

            // 3. 获取数据库记录
            List<FilePO> dbFiles = fileMapper.selectList(null);
            Map<String, FilePO> dbMap = new HashMap<>();
            for (FilePO file : dbFiles) {
                // 统一数据库中的路径格式
                String normalizedLocation = normalizePath(file.getDirectory() + "/" + file.getFileName());
                file.setDirectory(normalizePath(file.getDirectory()));
                dbMap.put(normalizedLocation, file);
            }

            // 4. 同步处理
            syncFiles(fileSystemMap, dbMap);

            System.out.println("文件同步完成，共处理文件: " + fileSystemMap.size());

        } catch (Exception e) {
            System.err.println("文件同步失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 路径标准化：统一使用正斜杠
    private String normalizePath(String path) {
        if (path == null) return null;
        // 将Windows的反斜杠替换为正斜杠
        return path.replace('\\', '/');
    }

    // 扫描目录
    private void scanDirectory(File dir, Map<String, FileInfo> resultMap) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            // 统一路径格式为Windows反斜杠（根据您存储的格式）
            String path = normalizePath(file.getAbsolutePath());
            FileInfo info = new FileInfo();
            info.path = path;
            info.size = file.length();
            info.lastModified = file.lastModified();
            info.isDirectory = file.isDirectory();

            resultMap.put(path, info);

            // 递归扫描子目录
            if (file.isDirectory()) {
                scanDirectory(file, resultMap);
            }
        }
    }

    // 同步文件系统与数据库
    private void syncFiles(Map<String, FileInfo> fileSystemMap, Map<String, FilePO> dbMap) {
        // 处理新增和修改的文件
        for (Map.Entry<String, FileInfo> entry : fileSystemMap.entrySet()) {
            String path = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            File file = new File(path);

            if (dbMap.containsKey(path)) {
                // // 检查文件是否被修改
                // FilePO dbFile = dbMap.get(path);
                // if (dbFile.getFileSize() != fileInfo.size ||
                //         dbFile.getLastModified() == null ||
                //         dbFile.getLastModified().getTime() != fileInfo.lastModified) {
                //     // 更新文件信息
                //     dbFile.setFileSize(fileInfo.size);
                //     dbFile.setLastModified(new Date(fileInfo.lastModified));
                //     fileMapper.updateById(dbFile);
                // }
            } else {
                // 新增文件记录
                FilePO newFile = new FilePO();
                newFile.setFileName(file.getName());
                newFile.setFileSize(fileInfo.size);
                newFile.setDirectory(normalizePath(file.getParent()));
                newFile.setIsDir(fileInfo.isDirectory ? 1 : 0);
                // newFile.setLastModified(new Date(fileInfo.lastModified));
                fileMapper.insert(newFile);
            }
        }

        // 处理已删除的文件
        for (Map.Entry<String, FilePO> entry : dbMap.entrySet()) {
            String path = entry.getKey();
            if (!fileSystemMap.containsKey(path)) {
                // 数据库中有但文件系统中已删除
                fileMapper.delete(new LambdaQueryWrapper<FilePO>().apply("CONCAT(directory, '/', file_name) = {0}", path));
            }
        }
    }

    // 文件信息类
    private static class FileInfo {
        String path;
        long size;
        long lastModified;
        boolean isDirectory;
    }
}
