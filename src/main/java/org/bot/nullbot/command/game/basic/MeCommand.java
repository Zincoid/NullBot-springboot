package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Me", "me", "个人信息"})
@Component
@RequiredArgsConstructor
@Slf4j
public class MeCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        UserPO user = userService.getUser(event.getUserId());
        bot.sendGroupMsg(event.getGroupId(), user.toString(), false);
        log.info("\t\t\t\t├─[Me] 已获取个人信息 - {}({})", user.getName(), user.getId());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Me 命令
                功能: 展示个人信息
                限权: %d 级
                格式: Me
                item: me/个人信息""", getAccess()
        );
    }
}
