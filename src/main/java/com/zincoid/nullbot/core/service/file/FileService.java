package com.zincoid.nullbot.core.service.file;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.query.FileQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.model.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService extends IService<FilePO> {

    List<FilePO> search(String key, String fullDir);

    FileInfo upload(String url, String directory, String fileName, Long ownerId, String ownerName);

    boolean delete(String directory, String fileName);

    boolean initRoot();

    void syncFiles();

    PageResult<FilePO> getPage(FileQuery query);

    List<FilePO> search(String key, String curDir, boolean hidden);

    void upload(Long owner, MultipartFile uploadFile, String curDir) throws IOException;

    void delete(Integer id);

    void download(Integer id, HttpServletRequest request, HttpServletResponse response);

    void createDir(Long ownerId, String curDir, String dirName) throws IOException;

    void rename(Integer id, String newFileName);

    void move(Integer id, String newDir);

    void setVisible(Integer id, boolean visible);
}
