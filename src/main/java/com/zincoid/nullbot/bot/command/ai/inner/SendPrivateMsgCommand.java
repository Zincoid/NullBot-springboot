package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"4ed1314d"})  // 加密 仅供AI嵌入调用
@Component
@Slf4j
public class SendPrivateMsgCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.size() < 2)
            throw new NullBotMsgException("[私信] ❌参数不足");

        long qqNumber;
        try {
            qqNumber = Long.parseLong(params.getFirst());
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[私信] ❌参数格式错误");
        }

        String message = String.join(" ", params.subList(1, params.size()));  // 拼接信息
        bot.sendPrivateMsg(qqNumber, message, false);
        log.info("\t\t\t\t├─[SendPrivateMsg] 私信已发送 - {} -> {}", qqNumber, message);
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

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
