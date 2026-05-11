package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.entity.page.DataPage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileService {

    Boolean addFileRecordForBot(String directory, String fileName, Long fileSize, LocalDateTime lastModified, Long ownerId, String ownerName);

    Boolean deleteFileRecordForBot(String directory, String fileName);

    Boolean initRootFile();

    void syncFilesToDatabase();

    DataPage<FilePO> getFileByPage(Integer currentPage, Integer pageSize, String curDir, Boolean hidden);

    DataPage<FilePO> searchFile(String key, String curDir, Boolean hidden);

    Boolean upload(Long owner, MultipartFile uploadFile, String curDir) throws IOException;

    void download(Integer id, HttpServletRequest request, HttpServletResponse response);

    Boolean createDir(Long ownerId, String curDir, String dirName) throws IOException;

    Boolean deleteFile(Integer id);

    Boolean renameFile(Integer id, String newFileName);

    Boolean moveFile(Integer id, String newDir);

    Boolean setVisible(Integer id, Boolean visible);
}
