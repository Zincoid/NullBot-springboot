package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Me", "me", "个人信息"})
@Component
@RequiredArgsConstructor
public class MeCommand implements Command {

    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        UserPO user = userService.get(event.getUserId());
        bot.sendGroupMsg(event.getGroupId(), user.toString(), false);
        log.info("☑ [Me] 个人信息已获取 - UserId: {}", user.getId());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Me 命令
                功能: 展示个人信息
                限权: %d 级
                格式: Me
                别名: me/个人信息""", getAccess()
        );
    }
}
