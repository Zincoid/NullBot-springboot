package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

@CommandMapping({"b6713262"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallUserCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().isEmpty()) throw new NullBotMsgException("[撤回用户消息] ❌参数不足");
            try {
                int messageId = Integer.parseInt(event.getCommandParameters().getFirst());
                bot.deleteMsg(messageId);
                log.info("\t\t\t\t├─[RecallUser] 已撤回用户消息 -> Message Id: {}", messageId);
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[撤回用户消息] ❌参数格式错误");
            }
        }else
            throw new NullBotLogException("[撤回用户消息] ❌未设计 - 非群消息事件响应方式");
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
