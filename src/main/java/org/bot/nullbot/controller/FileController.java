package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.page.FilePage;
import org.bot.nullbot.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nullbot/file")
public class FileController
{
    private final FileService fileService;

    @GetMapping("/{currentPage}/{pageSize}")
    public WebResult getFileByPage(@PathVariable Integer currentPage,
                                @PathVariable Integer pageSize,
                                @RequestParam(defaultValue = "/") String curDir){
        FilePage filePage = fileService.getFileByPage(currentPage, pageSize, curDir);
        return WebResult.success().addMsg("查询成功.").addData("filePage", filePage);
    }

    @GetMapping("/searchFile")
    public WebResult searchFile(String key, String curDir){
        if (key.contains("/") || key.contains("\\")){
            return WebResult.fail().addMsg("不允许出现斜杠");
        }
        FilePage filePage = fileService.searchFile(key, curDir);
        return WebResult.success().addMsg("查询成功.").addData("filePage", filePage);
    }

    @PostMapping("/upload")
    public WebResult upload(MultipartFile uploadFile, @RequestParam(defaultValue = "/") String curDir){
        return fileService.upload(uploadFile, curDir);
    }

    @GetMapping("/download/{id}")
    public WebResult download(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response){
        return fileService.download(id, request, response);
    }

    @PostMapping("/createDir")
    public WebResult createDir(@RequestBody Map<String, String> map){
        String curDir = map.get("curDir");
        String dirName = map.get("dirName");
        return fileService.createDir(curDir, dirName);
    }

    @DeleteMapping("/{id}")
    public WebResult deleteFile(@PathVariable Integer id){
        return fileService.deleteFile(id);
    }
}
