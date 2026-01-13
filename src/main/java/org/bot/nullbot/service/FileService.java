package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.page.FilePage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileService
{
    Boolean addFileRecordForBot(String directory, String fileName, Long fileSize, LocalDateTime lastModified, Long ownerId, String ownerName);

    Boolean initRootFile();

    void syncFilesToDatabase();

    FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir, Boolean hidden);

    FilePage searchFile(String key, String curDir, Boolean hidden);

    Boolean upload(MultipartFile uploadFile, String curDir) throws IOException;

    void download(Integer id, HttpServletRequest request, HttpServletResponse response);

    Boolean createDir(String curDir, String dirName) throws IOException;

    Boolean deleteFile(Integer id);

    Boolean renameFile(Integer id, String newFileName);

    Boolean moveFile(Integer id, String newDir);

    Boolean setVisible(Integer id, Boolean visible);
}
