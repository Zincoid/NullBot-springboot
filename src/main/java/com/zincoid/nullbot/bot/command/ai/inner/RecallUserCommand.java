package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"b6713262"})  // 加密 仅供AI调用
@Component
@RequiredArgsConstructor
public class RecallUserCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotException("[撤回用户消息] ❌参数不足");
        try {
            int messageId = Integer.parseInt(params.getFirst());
            bot.deleteMsg(messageId);
            log.info("├─[RecallUser] 已撤回用户消息 -> Message Id: {}", messageId);
        } catch (NumberFormatException e) {
            throw new NullBotException("[撤回用户消息] ❌参数格式错误");
        }
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
