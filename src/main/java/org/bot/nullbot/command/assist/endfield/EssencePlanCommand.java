package org.bot.nullbot.command.assist.endfield;

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

@CommandMapping({"EssencePlan", "基质规划", "基质"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EssencePlanCommand implements Command
{
    private final WebScreenCapturer webScreenCapturer;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[基质规划] ❌参数不足");

            String weapon = String.join(" ", params.subList(0, params.size()));
            String base64;

            try {
                base64 = webScreenCapturer.capture(
                        "https://end.canmoe.com/", 1024, 5120,
                        List.of("#app"),
                        List.of(),
                        List.of()
                );

            } catch (Exception e) {
                throw new NullBotMsgException("[基质规划] ❌查询失败: " + e.getMessage());
            }

            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[EssencePlan] 已查询 - {}", weapon);
        }else
            throw new NullBotLogException("[基质规划] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ EssencePlan 命令
                功能: 终末地基质刷取方案推荐
                限权: %d 级
                格式: EssencePlan [武器名]
                别名: 基质规划/基质""", getAccess()
        );
    }
}
