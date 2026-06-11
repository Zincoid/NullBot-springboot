package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"RawMsg", "原消息"})
@Component
@RequiredArgsConstructor
public class RawMsgCmd implements Cmd {

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply) throw new BotWarnException("需引用消息");
        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
        log.info("☑ [RawMsg] 原消息已输出:\n{}", replyMsg.getRawMessage());
        bot.sendGroupMsg(event.getGroupId(), "✅原消息已输出至控制台", false);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RawMsg 命令
                功能: 输出原消息至控制台
                限权: %d 级
                格式: [引用] RawMsg
                别名: 原消息""", getAccess()
        );
    }
}
