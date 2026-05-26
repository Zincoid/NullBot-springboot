package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.SayingService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"SayingDel", "删除语录"})
@Component
@RequiredArgsConstructor
public class SayingDeleteCommand implements Command {

    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        int id = params.nextInt();
        boolean deleted = sayingService.deleteById(id);
        bot.sendGroupMsg(event.getGroupId(), "[删除语录] ⚠️No.%s %s".formatted(id, deleted ? "已删除" : "不存在"), false);
        log.info("☑ [SayingDelete] 执行语录删除 - No.{} -> {}", id, deleted ? "已删除" : "不存在");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingDelete 命令
                功能: 删除语录
                限权: %d 级
                格式: SayingDelete [语录ID]
                别名: 删除语录""", getAccess()
        );
    }
}
