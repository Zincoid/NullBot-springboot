package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Recall", "recall", "rc", "撤回"})
@Component
@RequiredArgsConstructor
public class RecallCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("需引用消息");
        int messageId = reply.getData().get("id").asInt();
        bot.deleteMsg(messageId);
        log.info("☑ [Recall] 引用消息已撤回 - MessageId: {}", messageId);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Recall 命令
                功能: 撤回任意引用的消息
                限权: %d 级
                格式: [引用消息] Recall
                别名: recall/rc/撤回""", getAccess()
        );
    }
}
