package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.component.control.CommandRateLimiter;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.system.SettingService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/setting")
@RestController
public class SettingController {

    private final SettingService settingService;
    private final CommandRateLimiter commandRateLimiter;
    private final QQAiClient qqAiClient;

    public SettingController(SettingService settingService, CommandRateLimiter commandRateLimiter, @Lazy QQAiClient qqAiClient) {
        this.settingService = settingService;
        this.commandRateLimiter = commandRateLimiter;
        this.qqAiClient = qqAiClient;
    }

    @GetMapping("/{id}")
    public WebResult<SettingPO> get(@PathVariable Long id) {
        SettingPO setting = settingService.get(id);
        if (setting != null) {
            return WebResult.success("获取成功", setting);
        } else {
            return WebResult.fail("获取失败");
        }
    }

    @PutMapping("/set")
    public WebResult<Void> set(@RequestBody SettingPO setting) {
        Long groupId = setting.getGroupId();
        ChatScope oldScope = settingService.get(groupId).getChatScope();
        if (settingService.set(setting)) {
            if (oldScope != ChatScope.PERSONAL)
                qqAiClient.clear(oldScope + "_" + groupId);
            commandRateLimiter.reset(groupId);
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
