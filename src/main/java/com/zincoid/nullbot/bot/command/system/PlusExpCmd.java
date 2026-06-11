package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"PlusExp", "加经验"})
@Component
@RequiredArgsConstructor
public class PlusExpCmd implements Cmd {

    private final UserService userService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        long userId = args.nextLong();
        int exp = args.nextInt();
        if (!userService.exist(userId)) throw new BotInfoException(Emoji.INFO, "用户不存在");
        int i = userService.plusExperience(userId, exp);
        String userName = event.getSender().getNickname();
        StringBuilder sb = new StringBuilder("✅%s获得%sExp...".formatted(userName, exp));
        while (i-- > 0) sb.append("\n- LEVEL UP！");
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("☑ [PlusExp] 经验已给予 - {} -> {}Exp", userId, exp);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PlusExp 命令
                功能: 给予经验值
                限权: %d 级
                格式: PlusExp [QQ号] [经验值]
                别名: 加经验""", getAccess()
        );
    }
}
