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
import com.zincoid.nullbot.core.context.WebCtx;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequestMapping("/nullbot/files")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/sync")
    public WebResult<Void> sync() {
        WebCtx.requireAdmin();
        fileService.sync();
        return WebResult.success("本地与数据库 已同步");
    }

    @GetMapping
    public WebResult<PageResult<FilePO>> page(FileQuery query) {
        query.setHidden(WebCtx.getType() == 0);
        PageResult<FilePO> filePage = fileService.page(query);
        return WebResult.success("查询成功", filePage);
    }

    @GetMapping("/search")
    public WebResult<List<FilePO>> search(
            String keyword,
            String directory
    ) {
        Integer userType = WebCtx.getType();
        List<FilePO> fileList = fileService
                .search(keyword, directory, userType == 0);
        return WebResult.success("查询成功", fileList);
    }

    @PostMapping
    public WebResult<Void> upload(
            MultipartFile file,
            @RequestParam(defaultValue = "/") String directory
    ) {
        WebCtx.requireAdmin();
        Long userId = WebCtx.getId();
        fileService.upload(file, directory, userId);
        return WebResult.success("上传成功");
    }

    @GetMapping("/{id}/download")
    public void download(
            @PathVariable Integer id,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        fileService.download(id, request, response);
    }

    @PostMapping("/dir")
    public WebResult<Void> mkdir(
            @RequestParam String directory,
            @RequestParam String name
    ) {
        WebCtx.requireAdmin();
        Long userId = WebCtx.getId();
        fileService.mkdir(directory, name, userId);
        return WebResult.success("创建成功");
    }

    @DeleteMapping("/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        WebCtx.requireAdmin();
        fileService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/{id}/name")
    public WebResult<Void> rename(
            @PathVariable Integer id,
            @RequestParam String filename
    ) {
        WebCtx.requireAdmin();
        fileService.rename(id, filename);
        return WebResult.success("重命名成功");
    }

    @PutMapping("/{id}/directory")
    public WebResult<Void> move(
            @PathVariable Integer id,
            @RequestParam String directory
    ) {
        WebCtx.requireAdmin();
        fileService.move(id, directory);
        return WebResult.success("移动成功");
    }

    @PutMapping("/{id}/visible")
    public WebResult<Void> visualize(
            @PathVariable Integer id,
            @RequestParam Boolean flag
    ) {
        WebCtx.requireAdmin();
        fileService.visualize(id, flag);
        return WebResult.success("设置成功");
    }
}
