package org.bot.nullbot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Recall", "recall", "rc", "撤回"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallCommand implements Command
{
    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new NullBotMsgException("[撤回] ❌需引用消息");
        int messageId = Integer.parseInt(reply.getData().get("id"));
        bot.deleteMsg(messageId);
        log.info("\t\t\t\t├─[Recall] 已撤回引用消息 - Message Id -> {}", messageId);
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
