package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.SysMsgManager;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"bab329aa"})  // 加密 仅供AI调用
@Component
@RequiredArgsConstructor
public class ForgetCommand implements Command {

    private final SysMsgManager sysMsgManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        forget(bot, event.getGroupId(), false, params.nextInt());
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        forget(bot, event.getUserId(), true, params.nextInt());
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs params) {
        if (event.getGroupId() != null) {
            forget(bot, event.getGroupId(), false, params.nextInt());
        } else {
            forget(bot, event.getUserId(), true, params.nextInt());
        }
    }

    private void forget(Bot bot, Long targetId, boolean isPrivate, int i) {
        if (isPrivate) {
            String removed = sysMsgManager.removeLongTermUserMemory(targetId, i);
            bot.sendPrivateMsg(targetId, """
                    [遗忘] \uD83D\uDCA1长时记忆已移除
                    - 内容: %s""".formatted(removed), false);
            log.info("☑ [Forget] 用户长时记忆已移除 - {} : {}", targetId, removed);
        } else {
            String removed = sysMsgManager.removeLongTermGroupMemory(targetId, i);
            bot.sendGroupMsg(targetId, """
                    [遗忘] \uD83D\uDCA1长时记忆已移除
                    - 内容: %s""".formatted(removed), false);
            log.info("☑ [Forget] 群聊长时记忆已移除 - {} : {}", targetId, removed);
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return """
                ◉ bab329aa 命令
                功能: 遗忘指定内容(移除长期记忆)
                格式: bab329aa [记忆序号]
                示例: bab329aa 0""";
    }
}
