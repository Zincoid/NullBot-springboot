package com.zincoid.nullbot.bot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.message.ChatMessage;
import com.zincoid.nullbot.core.component.chat.ChatMemory;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@CommandMapping({"RecallReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallReactCommand implements Command {

    private final ChatMemory chatMemory;

    @Override
    public void execute(Bot bot, GroupMsgDeleteNoticeEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Long operatorId = event.getOperatorId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String operatorName = bot.getStrangerInfo(operatorId, true).getData().getNickname();
        Integer messageId = event.getMessageId();

        for (ChatMessage chatMessage : chatMemory.getMonitorHistory(groupId)) {
            if (!Objects.equals(chatMessage.getMessageId(), messageId)) continue;
            if (userId.equals(operatorId)) {
                bot.sendGroupMsg(groupId, """
                            %s(%s)撤回了消息:
                            %s""".formatted(userName, userId, chatMessage.getContent()), false
                );
            } else {
                bot.sendGroupMsg(groupId, """
                            %s(%s)撤回了%s(%s)的消息:
                            %s""".formatted(operatorName, operatorId, userName, userId, chatMessage.getContent()), false
                );
            }
            log.info("\t\t\t\t├─[RecallReact] 已重发撤回消息 - {}", chatMessage.getContent());
            return;
        }

        throw new NullBotMsgException("[撤回反馈] ❌该消息已清理");
    }

    // 仅校验群限权
    @Override
    public Integer getAccess() { return -1; }

    // 特殊命令 无帮助
}
