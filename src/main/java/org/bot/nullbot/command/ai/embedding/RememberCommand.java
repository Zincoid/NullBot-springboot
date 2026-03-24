package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.SysMsgStorage;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"0167a25a"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class RememberCommand implements Command
{
    private final SysMsgStorage sysMsgStorage;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        addMemory(bot, params, event.getGroupId(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        addMemory(bot, params, event.getUserId(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (event.getGroupId() != null)
            addMemory(bot, params, event.getGroupId(), false);
        else
            addMemory(bot, params, event.getUserId(), true);
    }

    private void addMemory(Bot bot, List<String> params, Long targetId, boolean isPrivate) {
        if (params.isEmpty())
            throw new NullBotMsgException("[记忆] ❌参数不足");
        if (isPrivate) {
            if (!sysMsgStorage.addLongTermUserMemory(targetId, params.getFirst()))
                throw new NullBotMsgException("[记忆] ❌容量已满");
            bot.sendPrivateMsg(targetId, """
                    [记忆] \uD83D\uDCA1长时记忆已添加
                    - 内容: %s""".formatted(params.getFirst()), false);
            log.info("\t\t\t\t├─[Remember] 用户长时记忆已添加 - {} : {}", targetId, params.getFirst());
        } else {
            if (!sysMsgStorage.addLongTermGroupMemory(targetId, params.getFirst()))
                throw new NullBotMsgException("[记忆] ❌容量已满");
            bot.sendGroupMsg(targetId, """
                    [记忆] \uD83D\uDCA1长时记忆已添加
                    - 内容: %s""".formatted(params.getFirst()), false);
            log.info("\t\t\t\t├─[Remember] 群聊长时记忆已添加 - {} : {}", targetId, params.getFirst());
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return """
                ◉ 0167a25a 命令
                功能: 记住指定内容(添加长期记忆)
                格式: 0167a25a [记忆内容]
                示例: 0167a25a 要友好对待大家哦""";
    }
}
