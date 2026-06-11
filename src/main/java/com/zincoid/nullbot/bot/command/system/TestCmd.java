package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.render.RenderingService;
import com.zincoid.nullbot.core.service.system.StatsService;
import com.zincoid.nullbot.core.utils.DownloadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Test", "test", "测试"})
@Component
@RequiredArgsConstructor
public class TestCmd implements Cmd {

    private final StatsService statsService;
    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        Long times = statsService.getUsage(userId);
        String url = ShiroUtils.getUserAvatar(userId, 5);
        FileInfo fileInfo = DownloadUtil.save(url);
        String response = MsgUtils.builder()
                .img("base64://" + renderingService.usage(fileInfo.getPath(), times))
                .build();
        bot.sendGroupMsg(groupId, response, false);

        log.info("☑ [Test] 用户已使用 {} 次指令", times);
    }

    @Override
    public Integer getAccess() { return 0; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Test 命令
                功能: 测试
                限权: %d 级
                格式: 不固定
                别名: test/测试""", getAccess()
        );
    }
}
