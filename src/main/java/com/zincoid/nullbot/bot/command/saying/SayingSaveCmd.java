package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.utils.MsgUtil;
import com.zincoid.nullbot.core.service.base.SayingService;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"SayingSave", "保存语录"})
@Component
@RequiredArgsConstructor
public class SayingSaveCmd implements Cmd {

    private final SayingService sayingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        MsgResp msg;
        if (args.hasOpt("id", "i")) {
            int messageId = args.optInt("id", "i");
            msg = bot.getMsg(messageId).getData();
        } else {
            ArrayMsg reply = event.getArrayMsg().getFirst();
            if (reply.getType() != MsgTypeEnum.reply)
                throw new BotWarnException("需引用文本");
            msg = bot.getMsg((int) reply.getLongData("id")).getData();
        }
        long userId = Long.parseLong(msg.getSender().getUserId());
        String userName = msg.getSender().getNickname();
        String text = MsgUtil.formatSaying(bot, msg.getArrayMsg());
        if (!sayingService.add(userId, userName, text))
            throw new BotErrorException("语录保存出错");
        bot.sendGroupMsg(event.getGroupId(), "\uD83D\uDCBE语录已保存", false);
        log.info("☑ [SayingSave] 语录已保存 - {}: {}", userName, text);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingSave 命令
                功能: 保存语录
                限权: %d 级
                格式: [引用] SayingSave
                别名: 保存语录""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ SayingSave 命令
                功能: 保存语录
                格式: SayingSave [选项]
                选项: -i,--id=[消息ID]
                示例: SayingSave -i=123456
                注意: 保存用户逆天或搞笑发言""";
    }
}
