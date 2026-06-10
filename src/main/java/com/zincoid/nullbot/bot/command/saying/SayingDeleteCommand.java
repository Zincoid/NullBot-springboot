package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.basic.SayingService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"SayingDel", "删除语录"})
@Component
@RequiredArgsConstructor
public class SayingDeleteCommand implements Command {

    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        int id = args.nextInt();
        if (!sayingService.removeById(id)) throw new BotInfoException(Emoji.INFO, "语录不存在");
        bot.sendGroupMsg(event.getGroupId(), "⚠️语录No.%s已删除".formatted(id), false);
        log.info("☑ [SayingDelete] 语录已删除 -> No.{}", id);
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
