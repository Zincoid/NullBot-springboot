package org.bot.nullbot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.ChatStorage;
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
            Long userId = groupMsgDeleteNoticeEvent.getUserId();
            Long operatorId = groupMsgDeleteNoticeEvent.getOperatorId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            String operatorName = bot.getStrangerInfo(operatorId, true).getData().getNickname();
            Integer messageId = groupMsgDeleteNoticeEvent.getMessageId();
            Long groupId = groupMsgDeleteNoticeEvent.getGroupId();
            List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(groupId);
            for(ChatMessage chatMessage : chatMessages) {
                if(Objects.equals(chatMessage.getMessageId(), messageId)) {
                    if (userId.equals(operatorId)) {
                        bot.sendGroupMsg(groupId, userName + "(" + userId + ") 撤回了一条消息:\n" + chatMessage.getContent(), false);
                    }else{
                        bot.sendGroupMsg(groupId, operatorName + "(" + operatorId + ") 撤回了 "+ userName + "(" + userId + ") 的一条消息: " + chatMessage.getContent(), false);
                    }
                    log.info("\t\t\t\t├─[React.Recall] 已重发撤回的消息 - {}", bot.getMsg(messageId).getData().getRawMessage());
                    return;
                }
            }
            bot.sendGroupMsg(groupId, "[撤回记录] ❌该消息已被清理", false);
            log.info("\t\t\t\t├─[React.Recall] 该消息已被清理 - MessageId -> {}", messageId);
        }else
            log.info("\t\t\t\t├─[React.Recall] 未设计 - 非群消息撤回事件响应方式");
    }

    // 限权: 0
}
