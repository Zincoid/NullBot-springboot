package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Component;

@CommandMapping({"RandomSaying", "say", "随机语录", "语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomSayingCommand implements Command
{
    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            SayingPO saying = sayingService.getRand();
            if (saying != null) {
                String text = saying.toString();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), text, false);
                log.info("\t\t\t\t├─[Saying.Random] 已发送语录 - {}", text.replaceAll("\\R", " "));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌暂无语录", false);
                log.info("\t\t\t\t├─[Saying.Random] 暂无语录");
            }
        }else
            log.info("\t\t\t\t├─[Saying.Random] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return -1;
    }

    @Override
    public String getHelp() {
        return "◉ RandomSaying 或 say 命令\n功能: 随机语录\n限权: " + getAccess() + "\n格式: RandomSaying 或 say\n中文命令: 随机语录 或 语录";
    }
}
