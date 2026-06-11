package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"b6713262"})
@Component
@RequiredArgsConstructor
public class RecallUserCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        int messageId = args.nextInt();
        bot.deleteMsg(messageId);
        log.info("☑ [RecallUser] 用户消息已撤回 -> MessageId: {}", messageId);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ b6713262 命令
                功能: 撤回非AI发送的用户消息
                格式: b6713262 [Message ID]
                示例: b6713262 965922865
                注意: 已撤回的消息依然会存在于之后发给你的消息列表里
                撤回其他人的消息用这个，撤回你自己的消息不要用这个""";
    }
}
