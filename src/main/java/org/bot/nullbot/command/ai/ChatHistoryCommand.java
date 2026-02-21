package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatHistory", "聊天历史"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String history = deepSeekClient.getGroupHistory(groupId, userId);
        bot.sendGroupMsg(groupId, "[聊天历史] ✅已获取！\n" + history, false);
        log.info("\t\t\t\t├─[ChatHistory] 已获取 - 历史聊天记录");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatHistory 命令
                功能: 获取AI聊天历史
                限权: %d 级
                格式: ChatHistory
                别名: 聊天历史""", getAccess()
        );
    }
}
