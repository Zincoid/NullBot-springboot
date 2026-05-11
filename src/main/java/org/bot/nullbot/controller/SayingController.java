package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.bot.nullbot.util.CsvExportUtil;
import org.bot.nullbot.util.CsvImportUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/saying")
@RestController
@RequiredArgsConstructor
public class SayingController {

    private final SayingService sayingService;

    @GetMapping("/list")
    public WebResult getSayingList(){
        return WebResult.success().addMsg("查询成功").addData("sayings", sayingService.getSayingList());
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getSayingByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        SayingPage sayingPage = sayingService.getSayingByPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("sayingPage", sayingPage);
    }

    @GetMapping("/random")
    public WebResult random(){
        log.info("[管理系统] 获取随机语录");
        SayingPO saying = sayingService.getRand();
        if(saying != null){
            return WebResult.success().addMsg("获取成功").addData("saying", saying.toString());
        }else{
            return WebResult.fail().addMsg("获取失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id){
        if(sayingService.deleteById(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<SayingPO> sayings = sayingService.getSayingList();
        CsvExportUtil.exportToCsv(response, "Sayings_" + LocalDateTime.now(), sayings, SayingPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<SayingPO> sayings =  CsvImportUtil.importFromCsv(csvFile, SayingPO.class);
        sayingService.addSayings(sayings);
    }
}
