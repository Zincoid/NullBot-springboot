package org.bot.nullbot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"Recall", "rc", "撤回"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                int messageId = Integer.parseInt(reply.getData().get("id"));
                bot.deleteMsg(messageId);
                log.info("\t\t\t\t├─[Recall] 已撤回引用消息 -> Message Id: {}", messageId);
            } else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[撤回] ❌无消息引用", false);
                log.info("\t\t\t\t├─[Recall] 无消息引用");
            }
        }else
            log.info("\t\t\t\t├─[Recall] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Recall 命令
                功能: 撤回任意引用的消息
                限权: %d
                格式: [引用消息] Recall 或 [引用消息] rc
                中文命令: 撤回""", getAccess()
        );
    }
}
