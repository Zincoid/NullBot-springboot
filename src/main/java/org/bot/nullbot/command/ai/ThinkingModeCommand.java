package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"ThinkingMode", "思考模式"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ThinkingModeCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String thinking = deepSeekClient.changeThinking();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[思考模式] \uD83D\uDD04已切换至: " + thinking, false);
            log.info("\t\t\t\t├─[AI.ThinkingMode] 思考模式已切换 - {}", thinking);
        }else
            log.info("\t\t\t\t├─[AI.ThinkingMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ ThinkingMode 命令\n功能: 切换AI思考模式\n限权: " + getAccess() + "\n格式: ThinkingMode\n中文命令: 思考模式";
    }
}
