package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"db3fbe2b"})
@Component
@RequiredArgsConstructor
public class RecallAICommand implements Command {

    private final MsgWindowMemory msgWindowMemory;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        int n = args.nextInt(1);
        if (n <= 0) throw new BotWarnException("消息数非正");
        List<QQMessage> messages = msgWindowMemory.get(BotCtx.getChatId()).stream().map(m -> (QQMessage) m).toList();
        List<QQMessage> filtered = messages.stream().filter(msg -> msg.getRole() == Role.ASSISTANT && msg.getMessageId() != null).toList();
        List<QQMessage> targets = filtered.subList(Math.max(0, filtered.size() - n), filtered.size());
        for (QQMessage target : targets) bot.deleteMsg(target.getMessageId());
        log.info("☑ [RecallAI] AI消息已撤回 - Amount: {}", n);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ db3fbe2b 命令
                功能: 撤回AI发送的最近消息(仅文本消息)
                格式: db3fbe2b [可选: 条数(默认为1)]
                示例: db3fbe2b 1
                注意: 该指令使用时必须置于你此次所有回复内容之前
                已撤回的消息依然会存在于之后发给你的消息列表里
                撤回你自己的消息用这个，撤回其他人的消息不要用这个""";
    }
}
