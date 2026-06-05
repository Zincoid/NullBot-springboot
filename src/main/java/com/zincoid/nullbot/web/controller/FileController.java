package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.FileQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.util.WebCtxUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/file")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/init")
    public WebResult<Void> init() {
        if (fileService.init()) {
            return WebResult.success("初始化完成");
        } else {
            return WebResult.fail("已初始化过");
        }
    }

    @GetMapping("/sync")
    public WebResult<Void> sync() {
        fileService.sync();
        return WebResult.success("本地与数据库 已同步");
    }

    @GetMapping("/page")
    public WebResult<PageResult<FilePO>> page(FileQuery query) {
        query.setHidden(WebCtxUtil.getType() == 0);
        PageResult<FilePO> filePage = fileService.page(query);
        return WebResult.success("查询成功", filePage);
    }

    @GetMapping("/search")
    public WebResult<List<FilePO>> search(
            String keyword,
            String directory
    ) {
        Integer userType = WebCtxUtil.getType();
        List<FilePO> fileList = fileService.search(
                keyword, directory, userType == 0);
        return WebResult.success("查询成功", fileList);
    }

    @PostMapping("/upload")
    public WebResult<Void> upload(
            MultipartFile file,
            @RequestParam(defaultValue = "/") String directory
    ) throws IOException {
        Long userId = WebCtxUtil.getId();
        fileService.upload(file, directory, userId);
        return WebResult.success("上传成功");
    }

    @GetMapping("/download/{id}")
    public void download(
            @PathVariable Integer id,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        fileService.download(id, request, response);
    }

    @GetMapping("/mkdir")
    public WebResult<Void> mkdir(
            String directory,
            String name
    ) {
        Long userId = WebCtxUtil.getId();
        fileService.mkdir(directory, name, userId);
        return WebResult.success("创建成功");
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        fileService.delete(id);
        return WebResult.success("删除成功");
    }

    @GetMapping("/rename/{id}")
    public WebResult<Void> rename(
            @PathVariable Integer id,
            @RequestParam String filename
    ) {
        fileService.rename(id, filename);
        return WebResult.success("重命名成功");
    }

    @GetMapping("/move/{id}")
    public WebResult<Void> move(
            @PathVariable Integer id,
            @RequestParam String directory
    ) {
        fileService.move(id, directory);
        return WebResult.success("移动成功");
    }

    @GetMapping("/visualize/{id}")
    public WebResult<Void> visualize(
            @PathVariable Integer id,
            @RequestParam Boolean flag
    ) {
        fileService.visualize(id, flag);
        return WebResult.success("设置成功");
    }
}
