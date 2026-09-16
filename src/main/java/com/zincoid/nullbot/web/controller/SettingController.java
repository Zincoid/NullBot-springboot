package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.enums.setting.ChatScope;
import com.zincoid.nullbot.core.model.data.dto.SettingDTO;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.control.CmdRateLimiter;
import com.zincoid.nullbot.core.service.system.SettingService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/settings")
@RestController
public class SettingController {

    private final SettingService settingService;
    private final CmdRateLimiter cmdRateLimiter;
    private final QQChatClient qqChatClient;

    public SettingController(SettingService settingService,
                             CmdRateLimiter cmdRateLimiter,
                             @Lazy QQChatClient qqChatClient) {
        this.settingService = settingService;
        this.cmdRateLimiter = cmdRateLimiter;
        this.qqChatClient = qqChatClient;
    }

    @GetMapping("/{id}")
    public WebResult<SettingPO> get(@PathVariable Long id) {
        SettingPO setting = settingService.get(id);
        return WebResult.success("获取成功", setting);
    }

    @PutMapping("/{groupId}")
    public WebResult<Void> set(@PathVariable Long groupId, @RequestBody @Valid SettingDTO setting) {
        WebCtx.requireAdmin();
        setting.setGroupId(groupId);
        ChatScope oldScope = settingService.get(groupId).getChatScope();
        settingService.set(setting);
        if (oldScope != ChatScope.PERSONAL) qqChatClient.clear(oldScope + "_" + groupId);
        cmdRateLimiter.reset(groupId);
        return WebResult.success("更新成功");
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        WebCtx.requireAdmin();
        List<SettingPO> settings = settingService.getAll();
        CsvUtil.exportCsv(response, "Settings_" + LocalDateTime.now(), settings, SettingPO.class);
    }

    @PostMapping("/import")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        WebCtx.requireAdmin();
        List<SettingPO> settings = CsvUtil.importCsv(csvFile, SettingPO.class);
        settingService.setAll(settings);
    }
}
