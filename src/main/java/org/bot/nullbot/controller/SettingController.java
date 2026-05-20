package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.CommandRateLimiter;
import org.bot.nullbot.entity.po.Setting;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SettingService;
import org.bot.nullbot.util.CsvExportUtil;
import org.bot.nullbot.util.CsvImportUtil;
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
        Setting setting = settingService.get(id);
        if (setting != null) {
            return WebResult.success("获取成功").withData("setting", setting);
        } else {
            return WebResult.fail("获取失败");
        }
    }

    @PutMapping("/set")
    public WebResult setSetting(@RequestBody Setting setting) {
        if (settingService.set(setting)) {
            commandRateLimiter.reset(setting.getGroupId());
            deepSeekClient.clearGroupHistory(setting.getGroupId(), null);
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<Setting> settings = settingService.getAll();
        CsvExportUtil.exportToCsv(response, "Settings_" + LocalDateTime.now(), settings, Setting.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<Setting> settings = CsvImportUtil.importFromCsv(csvFile, Setting.class);
        settingService.setAll(settings);
    }
}
