package com.zincoid.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.component.storage.SysMsgStorage;
import com.zincoid.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"bab329aa"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class ForgetCommand implements Command {

    private final SysMsgStorage sysMsgStorage;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        removeMemory(bot, params, event.getGroupId(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        removeMemory(bot, params, event.getUserId(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (event.getGroupId() != null)
            removeMemory(bot, params, event.getGroupId(), false);
        else
            removeMemory(bot, params, event.getUserId(), true);
    }

    private void removeMemory(Bot bot, List<String> params, Long targetId, boolean isPrivate) {
        if (params.isEmpty())
            throw new NullBotMsgException("[遗忘] ❌参数不足");
        int i;
        try {
            i = Integer.parseInt(params.getFirst());
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[遗忘] ❌参数格式错误");
        }
        if (isPrivate) {
            String removed = sysMsgStorage.removeLongTermUserMemory(targetId, i);
            bot.sendPrivateMsg(targetId, """
                    [遗忘] \uD83D\uDCA1长时记忆已移除
                    - 内容: %s""".formatted(removed), false);
            log.info("\t\t\t\t├─[Forget] 用户长时记忆已移除 - {} : {}", targetId, removed);
        } else {
            String removed = sysMsgStorage.removeLongTermGroupMemory(targetId, i);
            bot.sendGroupMsg(targetId, """
                    [遗忘] \uD83D\uDCA1长时记忆已移除
                    - 内容: %s""".formatted(removed), false);
            log.info("\t\t\t\t├─[Forget] 群聊长时记忆已移除 - {} : {}", targetId, removed);
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
