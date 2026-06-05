package com.zincoid.nullbot.core.service.file;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.query.FileQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.model.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<FilePO> {

    boolean init();

    void sync();

    PageResult<FilePO> page(FileQuery query);

    List<FilePO> list(String directory);

    List<FilePO> search(String keyword, String directory);

    List<FilePO> search(String keyword, String directory, boolean hidden);

    FileInfo upload(String url, String directory, String filename, Long uid);

    void upload(MultipartFile file, String directory, Long uid);

    void delete(String directory, String filename);

    void delete(Integer id);

    void download(Integer id, HttpServletRequest req, HttpServletResponse res);

    void mkdir(String directory, String name, Long uid);

    void rename(Integer id, String filename);

    void move(Integer id, String directory);

    void visualize(Integer id, boolean flag);
}
