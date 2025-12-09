package org.bot.nullbot.command.recall;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@CommandMapping({"RecallReact"})
@Component
public class RecallReactCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(RecallReactCommand.class);

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
            Long userId = groupMsgDeleteNoticeEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            Integer msgId = groupMsgDeleteNoticeEvent.getMessageId();
            Long groupId = groupMsgDeleteNoticeEvent.getGroupId();
            String recallMsg = bot.getMsg(msgId).getData().getRawMessage().replaceAll("\\[CQ:at,qq=(\\d+)\\]", "@$1").replaceAll("\\[CQ:.*?\\]", "");
            bot.sendGroupMsg(groupId, userName + "(" + userId + ") 撤回了一条消息: " + recallMsg, false);
            logger.info("\t\t\t\t├─[React.Recall] 已重发撤回的消息 - {}", recallMsg.replaceAll("\\R", ""));
        }else
            logger.info("\t\t\t\t├─[React.Recall] 未设计 - 非群消息事件响应方式");
    }

    // 限权: 0
}
