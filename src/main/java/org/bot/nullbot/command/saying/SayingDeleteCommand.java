package org.bot.nullbot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dao.mapper.SayingMapper;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"SayingDelete"})
@Component
@RequiredArgsConstructor
public class SayingDeleteCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(SayingDeleteCommand.class);
    private final SayingMapper sayingMapper;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (!event.getCommandParameters().isEmpty()){
                try {
                    int id = Integer.parseInt(event.getCommandParameters().get(0));
                    boolean deleted = sayingMapper.deleteById(id);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Saying.Delete] 执行语录删除: ID(" + id + ") -> " + (deleted ? "成功" : "无记录"), false);
                    logger.info("\t\t\t\t├─[Saying.Delete] 执行语录删除 - ID {} -> {}", id, deleted ? "成功" : "无记录");
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Saying.Delete] 参数格式错误", false);
                    logger.info("\t\t\t\t├─[Saying.Delete] 参数格式错误");
                }
            }else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Saying.Delete] 参数不足", false);
                logger.info("\t\t\t\t├─[Saying.Delete] 参数不足");
            }
        }else
            logger.info("\t\t\t\t├─[Saying.Delete] 无 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/SayingDelete 命令\n功能:  删除语录\n格式: /SayingDelete [语录ID]";
    }
}
