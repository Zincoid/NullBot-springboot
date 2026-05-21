package com.zincoid.nullbot.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.component.ai.DeepSeekClient;
import com.zincoid.nullbot.core.component.control.CommandRateLimiter;
import com.zincoid.nullbot.core.model.po.SettingPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.SettingService;
import com.zincoid.nullbot.core.util.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/setting")
@RestController
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final CommandRateLimiter commandRateLimiter;
    private final DeepSeekClient deepSeekClient;

    @GetMapping("/{id}")
    public WebResult getSetting(@PathVariable Long id) {
        SettingPO setting = settingService.get(id);
        if (setting != null) {
            return WebResult.success("获取成功").withData("setting", setting);
        } else {
            return WebResult.fail("获取失败");
        }
    }

    @PutMapping("/set")
    public WebResult setSetting(@RequestBody SettingPO setting) {
        if (settingService.set(setting)) {
            commandRateLimiter.reset(setting.getGroupId());
            deepSeekClient.clearGroupHistory(setting.getGroupId(), null);
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<SettingPO> settings = settingService.getAll();
        CsvUtil.exportCsv(response, "Settings_" + LocalDateTime.now(), settings, SettingPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<SettingPO> settings = CsvUtil.importCsv(csvFile, SettingPO.class);
        settingService.setAll(settings);
    }
}
