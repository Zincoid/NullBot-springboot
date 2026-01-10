package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.springframework.web.multipart.MultipartFile;

public interface FileService
{
    Boolean addFileRecordForBot(String directory, String fileName, Long fileSize);

    FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir, Boolean hidden);

    FilePage searchFile(String key, String curDir, Boolean hidden);

    WebResult upload(MultipartFile uploadFile, String curDir);

    WebResult download(Integer id, HttpServletRequest request, HttpServletResponse response);

    WebResult createDir(String curDir, String dirName);

    WebResult deleteFile(Integer id);

    WebResult renameFile(Integer id, String newFileName);

    WebResult setVisible(Integer id, Boolean visible);
}
