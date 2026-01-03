package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"b6713262-df1f-4627-a3a0-7e7bae50cb14"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallUserCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().isEmpty()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[撤回用户消息] ❌参数不足", false);
                log.info("\t\t\t\t├─[RecallUser] 参数不足");
                return;
            }
            try {
                int messageId = Integer.parseInt(event.getCommandParameters().getFirst());
                bot.deleteMsg(messageId);
                log.info("\t\t\t\t├─[RecallUser] 已撤回用户消息 -> Message Id: {}", messageId);
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[撤回用户消息] ❌参数格式错误", false);
                log.info("\t\t\t\t├─[RecallUser] 参数格式错误");
            }
        }else
            log.info("\t\t\t\t├─[RecallUser] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ b6713262-df1f-4627-a3a0-7e7bae50cb14 命令
                功能: 撤回非AI发送的用户消息
                限权: %d
                格式: b6713262-df1f-4627-a3a0-7e7bae50cb14 [Message ID]
                示例: b6713262-df1f-4627-a3a0-7e7bae50cb14 965922865
                注意: 已撤回的消息依然会存在于之后发给你的消息列表里！
                撤回用户消息必须用这个，不要用撤回AI消息的那个指令！""", getAccess()
        );
    }
}
