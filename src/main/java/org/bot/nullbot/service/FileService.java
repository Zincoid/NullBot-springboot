package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileService
{
    FilePage getFileByPage(Integer currentPage, Integer pageSize, String curDir);

    FilePage searchFile(String key, String curDir);

    WebResult upload(MultipartFile uploadFile, String curDir);

    WebResult download(Integer id, HttpServletRequest request, HttpServletResponse response);

    WebResult createDir(String curDir, String dirName);

    WebResult deleteFile(Integer id);
}
