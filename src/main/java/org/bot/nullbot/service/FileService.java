package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.entity.page.DataPage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileService {

    boolean addRecordOnly(String directory, String fileName, Long fileSize, LocalDateTime lastModified, Long ownerId, String ownerName);

    boolean deleteRecordOnly(String directory, String fileName);

    boolean initRoot();

    void syncLocalToDatabase();

    DataPage<FilePO> getPage(String curDir, Integer current, Integer size, boolean hidden);

    DataPage<FilePO> search(String key, String curDir, boolean hidden);

    void upload(Long owner, MultipartFile uploadFile, String curDir) throws IOException;

    void download(Integer id, HttpServletRequest request, HttpServletResponse response);

    void createDir(Long ownerId, String curDir, String dirName) throws IOException;

    void deleteById(Integer id);

    void rename(Integer id, String newFileName);

    void move(Integer id, String newDir);

    void setVisible(Integer id, boolean visible);
}
