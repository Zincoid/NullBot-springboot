package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@CmdMapping({"Test", "test", "测试"})
@Component
@RequiredArgsConstructor
public class TestCmd implements Cmd {

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Long selfId = event.getSelfId();
        String nickname = bot.getLoginInfo().getData().getNickname();

        // 构建消息列表（可以填充 MsgUtils 构建的消息）
        List<String> messages = new ArrayList<>() {{
            add("这是第一条消息");
            add("这是第二条消息");
            add("这是第三条消息");
        }};
        // 构建合并转发消息（selfId为合并转发消息显示的账号，nickname为显示的发送者昵称，msgList为消息列表）
        List<Map<String, Object>> forward = ShiroUtils.generateForwardMsg(selfId, nickname, messages);
        // 发送合并转发内容到群（groupId为要发送的群）
        bot.sendGroupForwardMsg(groupId, forward);

        log.info("☑ [Test] 测试: {}", "");
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
