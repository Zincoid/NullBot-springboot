package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
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

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        bot.sendGroupMsg(groupId, "暂无测试", false);

        bot.sendGroupMsg(groupId, """
                [测试] ✅测试结束
                - GroupID: %s
                - UserID: %s""".formatted(groupId, userId), false);
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
