package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowMemory;
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

    private final MsgWindowMemory msgWindowMemory;

    @Override
    public void execute(Bot bot, GroupMsgDeleteNoticeEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Long operatorId = event.getOperatorId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String operatorName = bot.getStrangerInfo(operatorId, true).getData().getNickname();
        Integer messageId = event.getMessageId();

        List<QQMessage> messages = msgWindowMemory.get(ChatScope.MONITOR + "_" + groupId)
                .stream().map(m -> (QQMessage) m).toList();
        for (QQMessage message : messages) {
            if (!Objects.equals(message.getMessageId(), messageId)) continue;
            String content = message.getContent();
            String response = userId.equals(operatorId)
                    ? "%s(%s)撤回消息: %s".formatted(userName, userId, content)
                    : "%s(%s)撤回%s(%s)消息: %s".formatted(operatorName, operatorId, userName, userId, content);
            bot.sendGroupMsg(groupId, response, false);
            log.info("☑ [RecallReact] 撤回消息已重发: {}", content);
            return;
        }

        // throw new BotWarnException("该消息已清理");
    }

    @Override
    public Integer getAccess() { return -1; }  // 仅用于群限权校验
}
