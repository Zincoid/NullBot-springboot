package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.GroupService;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"DbUpdate", "数据库更新"})
@Component
@RequiredArgsConstructor
public class DbUpdateCommand implements Command {

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {

        // 自定义更新方式
        groupService.updateAllNames();
        userService.updateAllNames();

        bot.sendGroupMsg(event.getGroupId(), "✅数据库已更新", false);
        log.info("☑ [DbUpdate] 数据库已更新");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DbUpdate 命令
                功能: 更新数据库条目
                限权: %d 级
                格式: DbUpdate
                别名: 数据库更新""", getAccess()
        );
    }
}
