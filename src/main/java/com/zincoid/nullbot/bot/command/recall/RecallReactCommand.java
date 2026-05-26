package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@CommandMapping({"RecallReact"})
@Component
@RequiredArgsConstructor
public class RecallReactCommand implements Command {

    private final MsgWindowChatMemory msgWindowChatMemory;

    @Override
    public void execute(Bot bot, GroupMsgDeleteNoticeEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Long operatorId = event.getOperatorId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String operatorName = bot.getStrangerInfo(operatorId, true).getData().getNickname();
        Integer messageId = event.getMessageId();

        List<QQMessage> messages = msgWindowChatMemory.get(ChatScope.MONITOR + "_" + groupId)
                .stream().map(m -> (QQMessage) m).toList();

        for (QQMessage message : messages) {
            if (!Objects.equals(message.getMessageId(), messageId)) continue;
            if (userId.equals(operatorId)) {
                bot.sendGroupMsg(groupId, """
                        %s(%s)撤回了消息:
                        %s""".formatted(userName, userId, message.getContent()), false);
            } else {
                bot.sendGroupMsg(groupId, """
                        %s(%s)撤回了%s(%s)的消息:
                        %s""".formatted(operatorName, operatorId, userName, userId, message.getContent()), false);
            }
            log.info("☑ [RecallReact] 撤回消息已重发: {}", message.getContent());
            return;
        }

        // throw new NullBotException("该消息已清理");
    }

    @Override
    public Integer getAccess() { return -1; }  // 仅校验群限权
}
