package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.WebUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nullbot/file")
public class FileController
{
    private final JwtTool jwtTool;
    private final FileService fileService;

    @GetMapping("/init")
    public WebResult initRootFile(){
        if(fileService.initRootFile()){
            return WebResult.success().addMsg("Root 文件 初始化完成");
        }else{
            return WebResult.fail().addMsg("Root 文件 已初始化过");
        }
    }

    @GetMapping("/sync")
    public WebResult syncFilesToDatabase(){
        try {
            fileService.syncFilesToDatabase();
            return WebResult.success().addMsg("本地与数据库 已同步");
        } catch (Exception e) {
            return WebResult.fail().addMsg("本地与数据库 同步失败");
        }
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getFileByPage(@PathVariable Integer currentPage,
                                @PathVariable Integer pageSize,
                                @RequestParam(defaultValue = "/") String curDir){
        FilePage filePage = fileService.getFileByPage(
                currentPage, pageSize, curDir,
                jwtTool.getLoginType(WebUtil.getToken()) == 0
        );
        return WebResult.success().addMsg("查询成功").addData("filePage", filePage);
    }

    @GetMapping("/searchFile")
    public WebResult searchFile(String key, String curDir){
        if (key.contains("/") || key.contains("\\")){
            return WebResult.fail().addMsg("不允许出现斜杠");
        }
        FilePage filePage = fileService.searchFile(
                key, curDir,
                jwtTool.getLoginType(WebUtil.getToken()) == 0
        );
        return WebResult.success().addMsg("查询成功").addData("filePage", filePage);
    }

    @PostMapping("/upload")
    public WebResult upload(MultipartFile uploadFile, @RequestParam(defaultValue = "/") String curDir) {
        try {
            Long userId = jwtTool.getLoginId(WebUtil.getToken());
            if(fileService.upload(userId, uploadFile, curDir))
                return WebResult.success().addMsg("上传成功");
            else
                return WebResult.fail().addMsg("上传失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response){
        try {
            fileService.download(id, request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createDir")
    public WebResult createDir(@RequestBody Map<String, String> map) {
        String curDir = map.get("curDir");
        String dirName = map.get("dirName");
        try {
            Long userId = jwtTool.getLoginId(WebUtil.getToken());
            if(fileService.createDir(userId, curDir, dirName))
                return WebResult.success().addMsg("创建成功");
            else
                return WebResult.fail().addMsg("创建失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("创建失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult deleteFile(@PathVariable Integer id){
        try {
            if(fileService.deleteFile(id))
                return WebResult.success().addMsg("删除成功");
            else
                return WebResult.fail().addMsg("删除失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/rename/{id}")
    public WebResult renameFile(@PathVariable Integer id, @RequestParam(defaultValue = "") String newFileName){
        try {
            if(fileService.renameFile(id, newFileName))
                return WebResult.success().addMsg("重命名成功");
            else
                return WebResult.fail().addMsg("重命名失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("重命名失败: " + e.getMessage());
        }
    }

    @GetMapping("/move/{id}")
    public WebResult moveFile(@PathVariable Integer id, @RequestParam String newDir){
        try {
            if(fileService.moveFile(id, newDir))
                return WebResult.success().addMsg("移动成功");
            else
                return WebResult.fail().addMsg("移动失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("移动失败: " + e.getMessage());
        }
    }

    @GetMapping("/setVisible/{id}")
    public WebResult setVisible(@PathVariable Integer id, @RequestParam Boolean visible){
        try {
            if(fileService.setVisible(id, visible))
                return WebResult.success().addMsg("设置成功");
            else
                return WebResult.fail().addMsg("设置失败: 未知错误");
        } catch (Exception e) {
            return WebResult.fail().addMsg("设置失败: " + e.getMessage());
        }
    }
}
