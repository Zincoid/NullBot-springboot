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
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                long userId = Long.parseLong(replyMsg.getSender().getUserId());
                String userName = replyMsg.getSender().getNickname();
                String text = MessageParseUtil.parseRawSaying(bot, replyMsg.getRawMessage());
                if(text != null) {
                    if(!text.trim().isEmpty()){
                        int inserted = sayingService.insert(userId, userName, text);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] " + (inserted == 1 ? "\uD83D\uDCBE已记录！" : "❌出错"), false);
                        log.info("\t\t\t\t├─[Saying.Save] 语录保存 - {}", (inserted == 1 ? "已记录 -> " : "出错 -> ") + text);
                    }else{
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌保存文本为空", false);
                        log.info("\t\t\t\t├─[Saying.Save] 保存文本为空");
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] \uD83D\uDE2D禁止套娃！", false);
                    log.info("\t\t\t\t├─[Saying.Save] 试图保存已输出的语录 -> 已忽略");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语录] ❌需回复要保存的文本", false);
                log.info("\t\t\t\t├─[Saying.Save] 未指定消息");
            }
        }else
            log.info("\t\t\t\t├─[Saying.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingSave 命令
                功能: 保存语录
                限权: %d
                格式: [引用文本] SayingSave
                中文命令: 保存语录""", getAccess()
        );
    }
}
