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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService
{
    private final FileMapper fileMapper;
    private final FileStorageConfig fileStorageConfig;

    @Override
    public FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir) {
        String fullDir;
        if(curDir.equals("/")){
            fullDir = fileStorageConfig.getFileDirectory() + "/";
        }else{
            fullDir = fileStorageConfig.getFileDirectory() + curDir + "/";
        }
        Page<FilePO> page = new Page<>(currentPage, pageSize);
        Page<FilePO> filePage = fileMapper.selectPage(page, new LambdaQueryWrapper<FilePO>().eq(FilePO::getDirectory, fullDir));
        return new FilePage(filePage.getRecords(), filePage.getCurrent(), filePage.getPages(), filePage.getSize());
    }

    @Override
    public WebResult upload(MultipartFile uploadFile, String curDir) {
        String extention = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + extention;
        String fullDir;
        if(curDir.equals("/")){
            fullDir = fileStorageConfig.getFileDirectory() + "/";
        }else{
            fullDir = fileStorageConfig.getFileDirectory() + curDir + "/";
        }

        FilePO file = new FilePO();
        file.setFileName(uploadFile.getOriginalFilename());
        file.setFileSize(uploadFile.getSize());
        file.setDirectory(fullDir);
        file.setLocation(fullDir + newFileName);
        file.setIsDir(0);

        java.io.File file_dir = new java.io.File(fullDir);
        if (!file_dir.exists()) {
            return WebResult.fail().addMsg("目录不存在");
        }

        try {
            uploadFile.transferTo(new java.io.File(fullDir + newFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileMapper.addFile(file);
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
            fileInputStream = new FileInputStream(new java.io.File(file.getLocation()));
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
            fullDir = fileStorageConfig.getFileDirectory() + "/";
        }else{
            fullDir = fileStorageConfig.getFileDirectory() + curDir + "/";
        }

        FilePO file = new FilePO();
        file.setFileName(dirName);
        file.setFileSize(0L);
        file.setDirectory(fullDir);
        file.setLocation(fullDir + dirName);
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

        fileMapper.addFile(file);
        return WebResult.success().addMsg("创建成功");
    }

    @Override
    public WebResult deleteFile(Integer id) {
        FilePO file = fileMapper.selectById(id);
        String location = file.getLocation();
        fileMapper.deleteFile(location);
        deleteFileByDir(new java.io.File(location));
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
}
