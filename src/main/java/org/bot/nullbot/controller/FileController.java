package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.WebCtxUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/nullbot/file")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/init")
    public WebResult initRootFile() {
        if (fileService.initRoot()) {
            return WebResult.success("初始化完成");
        } else {
            return WebResult.fail("已初始化过");
        }
    }

    @GetMapping("/sync")
    public WebResult syncFilesToDatabase() {
        fileService.syncLocalToDatabase();
        return WebResult.success("本地与数据库 已同步");
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getFileByPage(
            @PathVariable Integer currentPage,
            @PathVariable Integer pageSize,
            @RequestParam(defaultValue = "/") String curDir
    ) {
        // Integer userType = jwtTool.getLoginType(WebUtil.getToken());  // 弃用
        Integer userType = WebCtxUtil.getType();
        DataPage<FilePO> filePage = fileService.getPage(
                curDir,
                currentPage,
                pageSize,
                userType == 0
        );
        return WebResult.success("查询成功").withData("filePage", filePage);
    }

    @GetMapping("/searchFile")
    public WebResult searchFile(String key, String curDir) {
        // Integer userType = jwtTool.getLoginType(WebUtil.getToken());  // 弃用
        Integer userType = WebCtxUtil.getType();
        List<FilePO> fileList = fileService.search(
                key,
                curDir,
                userType == 0
        );
        return WebResult.success("查询成功").withData("fileList", fileList);
    }

    @PostMapping("/upload")
    public WebResult upload(
            MultipartFile uploadFile,
            @RequestParam(defaultValue = "/") String curDir
    ) throws IOException {
        // Long userId = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
        Long userId = WebCtxUtil.getId();
        fileService.upload(userId, uploadFile, curDir);
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

    @PostMapping("/createDir")
    public WebResult createDir(@RequestBody Map<String, String> map) throws IOException {
        String curDir = map.get("curDir");
        String dirName = map.get("dirName");
        // Long userId = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
        Long userId = WebCtxUtil.getId();
        fileService.createDir(userId, curDir, dirName);
        return WebResult.success("创建成功");
    }

    @DeleteMapping("/delete/{id}")
    public WebResult deleteFile(@PathVariable Integer id) {
        fileService.deleteById(id);
        return WebResult.success("删除成功");
    }

    @GetMapping("/rename/{id}")
    public WebResult renameFile(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "") String newFileName
    ) {
        fileService.rename(id, newFileName);
        return WebResult.success("重命名成功");
    }

    @GetMapping("/move/{id}")
    public WebResult moveFile(
            @PathVariable Integer id,
            @RequestParam String newDir
    ) {
        fileService.move(id, newDir);
        return WebResult.success("移动成功");
    }

    @GetMapping("/setVisible/{id}")
    public WebResult setVisible(
            @PathVariable Integer id,
            @RequestParam Boolean visible
    ) {
        fileService.setVisible(id, visible);
        return WebResult.success("设置成功");
    }
}
