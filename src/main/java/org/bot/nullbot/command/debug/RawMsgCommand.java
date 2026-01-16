package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"RawMsg", "原始消息"})
@Component
@Slf4j
@RequiredArgsConstructor
public class RawMsgCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() != MsgTypeEnum.reply)
                throw new NullBotMsgException("[原始消息] ❌需引用消息");
            GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
            log.info("\t\t\t\t├─[RawMsg] 已获取");
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[原始消息] ✅已获取\n" + replyMsg.getRawMessage(), false);
        }else
            throw new NullBotLogException("[原始消息] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RawMsg 命令
                功能: 获取引用的原始消息
                限权: %d 级
                格式: RawMsg
                中文命令: 原始消息""", getAccess()
        );
    }
}
