package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Component;

@CommandMapping({"RandomSaying", "Saying", "saying", "say", "随机语录", "语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomSayingCommand implements Command
{
    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            SayingPO saying;
            if(event.getCommandParameters().isEmpty()){
                saying = sayingService.getRand();
            }else{
                try {
                    long qqNumber = Long.parseLong(event.getCommandParameters().getFirst());
                    saying = sayingService.getRandByUserId(qqNumber);
                } catch (NumberFormatException e) {
                    throw new NullBotMsgException("[随机语录] ❌参数格式错误");
                }
            }
            if (saying == null) throw new NullBotMsgException("[随机语录] ❌暂无用户记录");
            String text = saying.toString();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), text, false);
            log.info("\t\t\t\t├─[RandomSaying] 已发送语录 - {}", text.replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[随机语录] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomSaying 命令
                功能: 随机语录(可指定发言人)
                限权: %d 级
                格式: RandomSaying [可选: QQ号]
                别名: saying/say/随机语录/语录""", getAccess()
        );
    }
}
