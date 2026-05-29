package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.service.render.RenderingService;
import com.zincoid.nullbot.core.service.system.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Test", "test", "测试"})
@Component
@RequiredArgsConstructor
public class TestCommand implements Command {

    private final StatisticService statisticService;
    private final RenderingService renderingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        Long uses = statisticService.getUses(userId);
        String response = MsgUtils.builder()
                .img("base64://" + renderingService.uses(uses))
                .build();
        bot.sendGroupMsg(groupId, response, false);

        bot.sendGroupMsg(groupId, """
                ✅测试结束
                - GroupID: %s
                - UserID: %s""".formatted(groupId, userId), false);
        log.info("☑ [Test] 用户已使用 {} 次指令", uses);
    }

    @Override
    public Integer getAccess() { return 2; }

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
