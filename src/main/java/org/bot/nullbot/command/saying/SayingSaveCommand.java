package org.bot.nullbot.command.saying;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dao.mapper.SayingMapper;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@CommandMapping({"SayingSave", "保存语录"})
@Component
@RequiredArgsConstructor
public class SayingSaveCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(SayingSaveCommand.class);
    private final SayingMapper sayingMapper;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().get(0);
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                long userId = Long.parseLong(replyMsg.getSender().getUserId());
                String userName = replyMsg.getSender().getNickname();
                String text = replyMsg.getRawMessage().replaceAll("\\[CQ:at,qq=(\\d+)\\]", "@$1").replaceAll("\\[CQ:.*?\\]", "");
                if(!Pattern.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\]\\[No\\.\\d+\\][\\s\\S]*", ShiroUtils.unescape(text))){
                    if(!text.trim().isEmpty()){
                        int inserted = sayingMapper.insert(userId, userName, text);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] " + (inserted == 1 ? "\uD83D\uDCBE已记录" : "❌出错"), false);
                        logger.info("\t\t\t\t├─[Saying.Save] 语录保存 - {}", (inserted == 1 ? "已记录 ->" : "出错 ->") + text);
                    }else{
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌保存文本为空", false);
                        logger.info("\t\t\t\t├─[Saying.Save] 保存文本为空");
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] \uD83D\uDE2D禁止套娃！", false);
                    logger.info("\t\t\t\t├─[Saying.Save] 试图保存已输出的语录 -> 已忽略");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌需回复要保存的文本", false);
                logger.info("\t\t\t\t├─[Saying.Save] 未指定消息");
            }
        }else
            logger.info("\t\t\t\t├─[Saying.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ SayingSave 命令\n功能: 保存语录\n限权: " + getAccess() + "\n格式: [引用文本]SayingSave\n中文命令: 保存语录";
    }
}
