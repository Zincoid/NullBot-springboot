package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@CommandMapping({"RecallReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallReactCommand implements Command {

    private final MsgWindowChatMemory msgWindowChatMemory;

    @Override
    public void execute(Bot bot, GroupMsgDeleteNoticeEvent event, List<String> params) {
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
            log.info("├─[RecallReact] 已重发撤回消息 - {}", message.getContent());
            return;
        }

        throw new NullBotException("[撤回反馈] ❌该消息已清理");
    }

    // 仅校验群限权
    @Override
    public Integer getAccess() { return -1; }

    // 特殊命令 无帮助
}
