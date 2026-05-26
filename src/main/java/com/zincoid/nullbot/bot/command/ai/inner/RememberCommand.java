package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.SysMsgManager;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"0167a25a"})  // 加密 仅供AI调用
@Component
@RequiredArgsConstructor
public class RememberCommand implements Command {

    private final SysMsgManager sysMsgManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        remember(bot, event.getGroupId(), params.nextFullString(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        remember(bot, event.getUserId(), params.nextFullString(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs params) {
        if (event.getGroupId() != null) {
            remember(bot, event.getGroupId(), params.nextFullString(), false);
        } else {
            remember(bot, event.getUserId(), params.nextFullString(), true);
        }
    }

    private void remember(Bot bot, Long targetId, String content, boolean isPrivate) {
        if (isPrivate) {
            if (!sysMsgManager.addLongTermUserMemory(targetId, content))
                throw new NullBotException("记忆容量已满");
            bot.sendPrivateMsg(targetId, """
                    [记忆] \uD83D\uDCA1长时记忆已添加
                    - 内容: %s""".formatted(content), false);
            log.info("☑ [Remember] 用户记忆已添加 - {} -> {}", targetId, content);
        } else {
            if (!sysMsgManager.addLongTermGroupMemory(targetId, content))
                throw new NullBotException("记忆容量已满");
            bot.sendGroupMsg(targetId, """
                    [记忆] \uD83D\uDCA1长时记忆已添加
                    - 内容: %s""".formatted(content), false);
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
