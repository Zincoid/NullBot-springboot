package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileService
{
    Boolean addFileRecordForBot(String directory, String fileName, Long fileSize, LocalDateTime lastModified, Long ownerId, String ownerName);

    FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir, Boolean hidden);

    FilePage searchFile(String key, String curDir, Boolean hidden);

    WebResult upload(MultipartFile uploadFile, String curDir) throws IOException;

    WebResult download(Integer id, HttpServletRequest request, HttpServletResponse response);

    WebResult createDir(String curDir, String dirName) throws IOException;

    WebResult deleteFile(Integer id);

    WebResult renameFile(Integer id, String newFileName);

    WebResult setVisible(Integer id, Boolean visible);
}
