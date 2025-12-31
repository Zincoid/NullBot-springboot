package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
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
            if (!event.getCommandParameters().isEmpty()){
                try {
                    int id = Integer.parseInt(event.getCommandParameters().getFirst());
                    boolean deleted = sayingService.deleteById(id);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] \uD83D\uDDD1 No." + id + " -> " + (deleted ? "已删除！" : "无记录"), false);
                    log.info("\t\t\t\t├─[Saying.Delete] 执行语录删除 - No.{} -> {}", id, deleted ? "已删除" : "无记录");
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌删除参数格式错误", false);
                    log.info("\t\t\t\t├─[Saying.Delete] 删除参数格式错误");
                }
            }else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌删除参数不足", false);
                log.info("\t\t\t\t├─[Saying.Delete] 删除参数不足");
            }
        }else
            log.info("\t\t\t\t├─[Saying.Delete] 无 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingDelete 命令
                功能: 删除语录
                限权: %d
                格式: SayingDelete [语录ID]
                中文命令: 删除语录""", getAccess()
        );
    }
}
