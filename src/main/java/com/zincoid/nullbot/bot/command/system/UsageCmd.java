package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.render.RenderingService;
import com.zincoid.nullbot.core.service.system.StatsService;
import com.zincoid.nullbot.core.utils.SaveUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Usage", "用量"})
@Component
@RequiredArgsConstructor
public class UsageCmd implements Cmd {

    private final StatsService statsService;
    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Long times = statsService.getUsage(userId);
        FileInfo file = SaveUtil.save(ShiroUtils.getUserAvatar(userId, 5));
        String base64 = renderingService.usage(file.getPath(), times);
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [Usage] 指令用量 - {} -> {}", userId, times);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Usage 命令
                功能: 指令用量查询
                限权: %d 级
                格式: Usage
                别名: 用量""", getAccess()
        );
    }
}
