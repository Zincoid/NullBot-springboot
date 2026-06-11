package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.module.control.SysMsgManager;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"0167a25a"})
@Component
@RequiredArgsConstructor
public class RememberCommand implements Command {

    private final SysMsgManager sysMsgManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        remember(bot, event.getGroupId(), args.nextFullString(), false);
    }
    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) {
        remember(bot, event.getUserId(), args.nextFullString(), true);
    }
    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs args) {
        if (event.getGroupId() != null) {
            remember(bot, event.getGroupId(), args.nextFullString(), false);
        } else {
            remember(bot, event.getUserId(), args.nextFullString(), true);
        }
    }

    private void remember(Bot bot, Long targetId, String content, boolean isPrivate) {
        if (isPrivate) {
            if (!sysMsgManager.addUserMemory(targetId, content))
                throw new BotWarnException("记忆容量已满");
            bot.sendPrivateMsg(targetId, "\uD83D\uDCA1记忆已添加: %s".formatted(content), false);
            log.info("☑ [Remember] 用户记忆已添加 - {} -> {}", targetId, content);
        } else {
            if (!sysMsgManager.addGroupMemory(targetId, content))
                throw new BotWarnException("记忆容量已满");
            bot.sendGroupMsg(targetId, "\uD83D\uDCA1记忆已添加: %s".formatted(content), false);
            log.info("☑ [Remember] 群聊记忆已添加 - {} -> {}", targetId, content);
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ 0167a25a 命令
                功能: 记住指定内容(添加长期记忆)
                格式: 0167a25a [记忆内容]
                示例: 0167a25a 要友好对待大家哦""";
    }
}
