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

@CommandMapping({"DefenseMode", "防御模式", "防注入模式"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DefenseModeCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String antiInjection = deepSeekClient.changeAntiInjection();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[防御模式] \uD83D\uDD04已切换至: " + antiInjection, false);
            log.info("\t\t\t\t├─[AI.DefenseMode] 防御模式已切换 - {}", antiInjection);
        }else
            log.info("\t\t\t\t├─[AI.DefenseMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DefenseMode 命令
                功能: 切换AI防御模式
                限权: %d 级
                格式: DefenseMode
                中文命令: 防御模式/防注入模式""", getAccess()
        );
    }
}
