package org.bot.nullbot.command.prts;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.render.WebScreenCapturer;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Operator", "干员查询", "干员"})
@Component
@RequiredArgsConstructor
@Slf4j
public class OperatorCommand implements Command
{
    private final WebScreenCapturer webScreenCapturer;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[干员查询] ❌参数不足");
            String base64 = webScreenCapturer.captureFull("https://prts.wiki/w/" + params.getFirst());
            // String base64 = webScreenCapturer.captureElement(
            //         "https://prts.wiki/w/" + params.getFirst(),
            //         ".mw-content-ltr.mw-parser-output"
            // );
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Operator] 已查询");
        }else
            throw new NullBotLogException("[干员查询] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Operator 命令
                功能: 明日方舟PRTS干员查询
                限权: %d 级
                格式: Operator
                别名: 干员查询/干员""", getAccess()
        );
    }
}
