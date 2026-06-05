package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.SayingQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.service.basic.SayingService;
import com.zincoid.nullbot.core.util.CsvUtil;
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
    public WebResult<List<SayingPO>> getList() {
        List<SayingPO> sayings = sayingService.list();
        return WebResult.success("查询成功", sayings);
    }

    @GetMapping("/page")
    public WebResult<PageResult<SayingPO>> getPage(SayingQuery query) {
        PageResult<SayingPO> sayingPage = sayingService.page(query);
        return WebResult.success("查询成功", sayingPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        if (sayingService.removeById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<SayingPO> sayings = sayingService.list();
        CsvUtil.exportCsv(response, "Sayings_" + LocalDateTime.now(), sayings, SayingPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<SayingPO> sayings = CsvUtil.importCsv(csvFile, SayingPO.class);
        sayingService.saveBatch(sayings);
    }
}
