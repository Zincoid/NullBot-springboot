package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"RawMsg", "原始消息"})
@Component
@RequiredArgsConstructor
public class RawMsgCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply) throw new NullBotException("需引用消息");
        MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
        log.info("☑ [RawMsg] 原始消息已输出\n{}", replyMsg.getRawMessage());
        bot.sendGroupMsg(event.getGroupId(), "[原始消息] ✅已输出至控制台", false);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RawMsg 命令
                功能: 输出引用原始消息至控制台
                限权: %d 级
                格式: RawMsg
                别名: 原始消息""", getAccess()
        );
    }
}
