package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.storage.SysMsgStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"SysMsgMode", "系统消息模式"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMsgModeCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final SysMsgStorage sysMsgStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();

            deepSeekClient.clearHistory(groupId, userId);
            String isCustom = sysMsgStorage.changeCustom();

            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[系统消息] \uD83D\uDD04已切换至: " + isCustom, false);
            log.info("\t\t\t\t├─[AI.SysMsgMode] 系统消息模式已更新 - {}", isCustom);
        }else
            log.info("\t\t\t\t├─[AI.SysMsgMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SysMsgMode 命令
                功能: 切换AI系统消息模式(并清空历史)
                限权: %d
                格式: SysMsgMode
                中文命令: 系统消息模式""", getAccess()
        );
    }
}
