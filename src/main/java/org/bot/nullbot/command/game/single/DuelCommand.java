package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Duel", "斗蛐蛐"})
@Component
@Slf4j
@RequiredArgsConstructor
public class DuelCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final BotNextInputer botNextInputer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Duel 命令
                功能:
                限权: %d 级
                格式:
                别名: 斗蛐蛐""", getAccess()
        );
    }
}
