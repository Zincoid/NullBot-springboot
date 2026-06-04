package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import com.zincoid.nullbot.core.service.basic.SayingService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"SayingSave", "保存语录"})
@Component
@RequiredArgsConstructor
public class SayingSaveCommand implements Command {

    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply) throw new BotWarnException("需引用文本");
        MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
        long userId = Long.parseLong(replyMsg.getSender().getUserId());
        String userName = replyMsg.getSender().getNickname();
        String text = MsgParseUtil.formatSaying(bot, replyMsg.getRawMessage());
        if (!sayingService.addSaying(userId, userName, text)) throw new BotErrorException("语录保存出错");
        bot.sendGroupMsg(event.getGroupId(), "\uD83D\uDCBE语录已保存", false);
        log.info("☑ [SayingSave] 语录已保存 - {}: {}", userName, text);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingSave 命令
                功能: 保存语录
                限权: %d 级
                格式: [引用文本] SayingSave
                别名: 保存语录""", getAccess()
        );
    }
}
