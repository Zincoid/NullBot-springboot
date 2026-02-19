package org.bot.nullbot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.render.WebScreenCapturer;
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
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[基质规划] ❌未指定武器");
        String weapon = params.getFirst();
        // String weapon = String.join(" ", params.subList(0, params.size()));

        String base64;
        try {
            base64 = webScreenCapturer.capture(
                    "https://end.canmoe.com/", 1536, 5120,
                    List.of("//section[contains(@class,'panel')][.//h2[contains(text(),'方案推荐列表')]]"),
                    List.of(".ghost-button"),
                    List.of(
                            "#app > div > div > div.notice-footer > div.about-actions > button",
                            String.format(
                                    "//div[@class='weapon-name']" +
                                            "/div[@class='weapon-title' and text()='%s']" +
                                            "/ancestor::div[contains(@class,'weapon-item')]",
                                    weapon
                            ),
                            "//button[contains(.,'收起其他方案')]"
                    )
            );
        } catch (Exception e) {
            throw new NullBotMsgException("[基质规划] ❌查询失败: " + e.getMessage());
        }

        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[EssencePlan] 已查询 - {}", weapon);
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
