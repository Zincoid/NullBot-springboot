package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Component;

@CommandMapping({"SayingDel", "删除语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SayingDeleteCommand implements Command
{
    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().isEmpty())
                throw new NullBotMsgException("[删除语录] ❌参数不足");
            try {
                int id = Integer.parseInt(event.getCommandParameters().getFirst());
                boolean deleted = sayingService.deleteById(id);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[删除语录] ⚠️No." + id + (deleted ? " 已删除！" : " 无记录"), false);
                log.info("\t\t\t\t├─[SayingDelete] 执行语录删除 - No.{} -> {}", id, deleted ? "已删除" : "无记录");
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[删除语录] ❌参数格式错误");
            }
        }else
            throw new NullBotLogException("[删除语录] ❌未设计 - 非群消息事件响应方式");
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
