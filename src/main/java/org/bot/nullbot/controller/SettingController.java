package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SettingService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/setting")
@RequiredArgsConstructor
@Slf4j
public class SettingController
{
    private final SettingService settingService;

    @GetMapping("/{id}")
    public WebResult getSetting(@PathVariable Long id) {
        SettingInfo setting = settingService.getSetting(id);
        if(setting != null)
            return WebResult.success().addMsg("获取成功").addData("setting", setting);
        else
            return WebResult.fail().addMsg("获取失败");
    }

    @PutMapping("/update")
    public WebResult updateSetting(@RequestBody SettingInfo setting){
        if(settingService.updateSetting(setting))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }
}
