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
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Component;

import java.util.List;


@CommandMapping({"SayingSave", "保存语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SayingSaveCommand implements Command
{
    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new NullBotMsgException("[保存语录] ❌需引用文本");
        GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
        long userId = Long.parseLong(replyMsg.getSender().getUserId());
        String userName = replyMsg.getSender().getNickname();
        String text;
        try {
            text = MessageParseUtil.parseRawSaying(bot, replyMsg.getRawMessage());
        } catch (Exception e) {
            throw new NullBotMsgException("[保存语录] ❌" + e.getMessage());
        }
        int inserted = sayingService.addSaying(userId, userName, text);
        bot.sendGroupMsg(event.getGroupId(), inserted == 1 ? "\uD83D\uDCBE 已记录！" : "[保存语录] ❌出错", false);
        log.info("\t\t\t\t├─[SayingSave] 语录保存 - {} -> {}", text, inserted == 1 ? "已记录" : "出错");
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
