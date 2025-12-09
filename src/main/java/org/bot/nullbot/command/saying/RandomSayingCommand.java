package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dao.mapper.SayingMapper;
import org.bot.nullbot.dao.po.SayingPO;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"RandomSaying", "say", "随机语录", "语录"})
@Component
@RequiredArgsConstructor
public class RandomSayingCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(RandomSayingCommand.class);
    private final SayingMapper sayingMapper;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            SayingPO saying = sayingMapper.getRand();
            if (saying != null) {
                String text = saying.toString();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), text, false);
                logger.info("\t\t\t\t├─[Saying.Random] 已发送语录 - {}", saying);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[随机语录] 无语录", false);
                logger.info("\t\t\t\t├─[Saying.Random] 无语录");
            }
        }else
            logger.info("\t\t\t\t├─[Saying.Random] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ RandomSaying 或 say 命令\n功能: 随机语录\n限权: " + getAccess() + "\n格式: RandomSaying 或 say\n中文命令: 随机语录 或 语录";
    }
}
