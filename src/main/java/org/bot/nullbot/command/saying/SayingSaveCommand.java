package org.bot.nullbot.command.saying;

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
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Component;


@CommandMapping({"SayingSave", "保存语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SayingSaveCommand implements Command
{
    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() != MsgTypeEnum.reply)
                throw new NullBotMsgException("[保存语录] ❌需引用文本");
            GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
            long userId = Long.parseLong(replyMsg.getSender().getUserId());
            String userName = replyMsg.getSender().getNickname();
            String text = MessageParseUtil.parseRawSaying(bot, replyMsg.getRawMessage());
            if(text == null)
                throw new NullBotMsgException("[保存语录] \uD83D\uDE21禁止套娃！");
            if(text.trim().isEmpty())
                throw new NullBotMsgException("[保存语录] ❌禁止空文本");
            int inserted = sayingService.addSaying(userId, userName, text);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), inserted == 1 ? "\uD83D\uDCBE 已记录！" : "[保存语录] ❌出错", false);
            log.info("\t\t\t\t├─[SayingSave] 语录保存 - {} -> {}", text, inserted == 1 ? "已记录" : "出错");
        }else
            throw new NullBotLogException("[保存语录] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingSave 命令
                功能: 保存语录
                限权: %d 级
                格式: [引用文本] SayingSave
                中文命令: 保存语录""", getAccess()
        );
    }
}
