package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"4ed1314d"})  // 加密 仅供AI调用
@Component
public class SendPrivateMsgCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        long targetId = args.nextLong();
        String message = args.nextFullString();
        bot.sendPrivateMsg(targetId, message, false);
        log.info("☑ [SendPrivateMsg] 私信已发送 - {} <- {}", targetId, message);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ 4ed1314d 命令
                功能: 给指定用户发送私聊消息/发送通知
                格式: 4ed1314d [QQ号] [消息]
                示例: 4ed1314d 2660181154 你好
                注意: 任何人都可以调用该指令
                消息第一行要附加让你发送的人的昵称和ID""";
    }
}
