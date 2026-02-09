package org.bot.nullbot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


@CommandMapping({"RecallReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallReactCommand implements Command
{
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
            Long groupId = groupMsgDeleteNoticeEvent.getGroupId();
            Long userId = groupMsgDeleteNoticeEvent.getUserId();
            Long operatorId = groupMsgDeleteNoticeEvent.getOperatorId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            String operatorName = bot.getStrangerInfo(operatorId, true).getData().getNickname();
            Integer messageId = groupMsgDeleteNoticeEvent.getMessageId();

            List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(groupId);
            for(ChatMessage chatMessage : chatMessages) {
                if(Objects.equals(chatMessage.getMessageId(), messageId)) {
                    if (userId.equals(operatorId)) {
                        bot.sendGroupMsg(groupId, userName + "(" + userId + ") 撤回了:\n" + chatMessage.getContent(), false);
                    }else{
                        bot.sendGroupMsg(groupId, operatorName + "(" + operatorId + ") 撤回了 "+ userName + "(" + userId + ") 的消息:\n" + chatMessage.getContent(), false);
                    }
                    log.info("\t\t\t\t├─[RecallReact] 已重发撤回消息 - {}", bot.getMsg(messageId).getData().getRawMessage());
                    return;
                }
            }

            throw new NullBotMsgException("[撤回反馈] ❌该消息已清理");
        }else
            throw new NullBotLogException("[撤回反馈] ❌未设计 - 非群撤回事件响应方式");
    }

    // 仅校验群限权
    @Override
    public Integer getAccess() { return -1; }

    // 特殊命令 无帮助
}
