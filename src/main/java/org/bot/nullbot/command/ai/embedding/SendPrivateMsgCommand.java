package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"4ed1314d"})  // 加密 仅供AI嵌入调用
@Component
@Slf4j
public class SendPrivateMsgCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() < 2) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[私信] ❌参数不足", false);
                log.info("\t\t\t\t├─[SendPrivateMsg] 参数不足");
                return;
            }

            long qqNumber;
            try {
                qqNumber = Long.parseLong(event.getCommandParameters().getFirst());
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[私信] ❌参数格式错误", false);
                log.info("\t\t\t\t├─[SendPrivateMsg] 参数格式错误");
                return;
            }

            // 将第二个及之后的参数用空格拼接起来
            List<String> params = event.getCommandParameters();
            String message = String.join(" ", params.subList(1, params.size()));

            bot.sendPrivateMsg(qqNumber, message, false);
            log.info("\t\t\t\t├─[SendPrivateMsg] 私信已发送 - {} -> {}", qqNumber, message);
        } else {
            log.info("\t\t\t\t├─[SendPrivateMsg] 未设计非群消息事件响应方式");
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ 4ed1314d 命令
                功能: 给指定用户发送私聊消息/发送通知
                限权: %d
                格式: 4ed1314d [QQ号] [消息]
                示例: 4ed1314d 2660181154 你好！
                注意: 消息第一行要附加让你发送的人的昵称和ID！任何人都可以调用该指令！""", getAccess()
        );
    }
}
