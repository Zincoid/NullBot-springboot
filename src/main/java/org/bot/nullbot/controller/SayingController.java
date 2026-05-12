package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.DataPage;
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
        return WebResult.success().withMsg("查询成功").withData("sayings", sayingService.getList());
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getSayingByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        DataPage<SayingPO> sayingPage = sayingService.getPage(currentPage, pageSize);
        return WebResult.success().withMsg("查询成功").withData("sayingPage", sayingPage);
    }

    @GetMapping("/random")
    public WebResult random(){
        log.info("[管理系统] 获取随机语录");
        SayingPO saying = sayingService.getRand();
        if(saying != null){
            return WebResult.success().withMsg("获取成功").withData("saying", saying.toString());
        }else{
            return WebResult.fail().withMsg("获取失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id){
        if(sayingService.deleteById(id)){
            return WebResult.success().withMsg("删除成功");
        }else{
            return WebResult.fail().withMsg("删除失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<SayingPO> sayings = sayingService.getList();
        CsvExportUtil.exportToCsv(response, "Sayings_" + LocalDateTime.now(), sayings, SayingPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<SayingPO> sayings =  CsvImportUtil.importFromCsv(csvFile, SayingPO.class);
        sayingService.adds(sayings);
    }
}
