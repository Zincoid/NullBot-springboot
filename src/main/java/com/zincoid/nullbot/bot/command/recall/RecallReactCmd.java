package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.ai.chat.memory.MsgWindowMemory;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.enums.ChatScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@CmdMapping({"RecallReact"})
@Component
@RequiredArgsConstructor
public class RecallReactCmd implements Cmd {

    private final MsgWindowMemory msgWindowMemory;

    @Override
    public void run(Bot bot, GroupMsgDeleteNoticeEvent event, CmdArgs args) {
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
