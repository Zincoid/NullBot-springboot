package com.zincoid.nullbot.bot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.render.WebScreenCapturer;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"EssencePlan", "基质规划", "基质"})
@Component
@RequiredArgsConstructor
public class EssencePlanCommand implements Command {

    private final WebScreenCapturer webScreenCapturer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        String weapon = params.nextFullString();
        String base64 = webScreenCapturer.capture(
                "https://end.canmoe.com/", 1536, 5120,
                List.of("//section[contains(@class,'panel')][.//h2[contains(text(),'方案推荐列表')]]"),
                List.of(".ghost-button"),
                List.of(
                        "#app > div > div > div.notice-footer > div.about-actions > button",
                        String.format("//span[@class='weapon-title-text' and text()='%s']", weapon),
                        "//button[contains(.,'收起其他方案')]"
                )
        );
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [EssencePlan] 已查询 - {}", weapon);
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
