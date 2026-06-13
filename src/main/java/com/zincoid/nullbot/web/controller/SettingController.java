package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.enums.ChatScope;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.control.CmdRateLimiter;
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
    private final CmdRateLimiter cmdRateLimiter;
    private final QQChatClient qqChatClient;

    public SettingController(SettingService settingService, CmdRateLimiter cmdRateLimiter, @Lazy QQChatClient qqChatClient) {
        this.settingService = settingService;
        this.cmdRateLimiter = cmdRateLimiter;
        this.qqChatClient = qqChatClient;
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
                qqChatClient.clear(oldScope + "_" + groupId);
            cmdRateLimiter.reset(groupId);
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
