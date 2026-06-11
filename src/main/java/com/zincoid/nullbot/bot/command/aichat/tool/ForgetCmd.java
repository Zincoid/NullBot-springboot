package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.SysMsgManager;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"bab329aa"})
@Component
@RequiredArgsConstructor
public class ForgetCmd implements Cmd {

    private final SysMsgManager sysMsgManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        forget(bot, event.getGroupId(), false, args.nextInt());
    }
    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        forget(bot, event.getUserId(), true, args.nextInt());
    }
    @Override
    public void run(Bot bot, PokeNoticeEvent event, CmdArgs args) {
        if (event.getGroupId() != null) {
            forget(bot, event.getGroupId(), false, args.nextInt());
        } else {
            forget(bot, event.getUserId(), true, args.nextInt());
        }
    }

    private void forget(Bot bot, Long resourceId, boolean isPrivate, int i) {
        if (isPrivate) {
            String removed = sysMsgManager.removeUserMemory(resourceId, i);
            bot.sendPrivateMsg(resourceId, "\uD83D\uDCA1记忆已移除: %s".formatted(removed), false);
            log.info("☑ [Forget] 用户记忆已移除 - {} -> {}", resourceId, removed);
        } else {
            String removed = sysMsgManager.removeGroupMemory(resourceId, i);
            bot.sendGroupMsg(resourceId, "\uD83D\uDCA1记忆已移除: %s".formatted(removed), false);
            log.info("☑ [Forget] 群聊记忆已移除 - {} -> {}", resourceId, removed);
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ bab329aa 命令
                功能: 遗忘指定内容(移除长期记忆)
                格式: bab329aa [记忆序号]
                示例: bab329aa 0""";
    }
}
