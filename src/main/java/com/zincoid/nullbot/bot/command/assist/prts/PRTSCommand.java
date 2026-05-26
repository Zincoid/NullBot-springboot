package com.zincoid.nullbot.bot.command.assist.prts;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.render.WebScreenCapturer;
import com.zincoid.nullbot.bot.exception.NullBotException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"PRTS", "prts", "PRTS查询"})
@Component
@RequiredArgsConstructor
@Slf4j
public class PRTSCommand implements Command {

    private final WebScreenCapturer webScreenCapturer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotException("[PRTS] ❌参数不足");

        String option = params.getFirst();
        String keyword;
        String base64;

        try {
            if (List.of("语音", "档案", "密录", "悖论").contains(option)) {
                if (params.size() < 2)
                    throw new NullBotException("[PRTS] ❌参数不足");
                keyword = params.get(1);
                base64 = switch (option)
                {
                    case "语音" -> webScreenCapturer.capture(
                            "https://prts.wiki/w/" + keyword, 1024, 5120,
                            List.of("#voice-table-root"),
                            List.of(".backToTop", "#rightToc", ".z-1.float-right.select-none"),
                            List.of("a[class*='z-1 float-right select-none']")
                    );

                    case "档案" -> webScreenCapturer.capture(
                            "https://prts.wiki/w/" + keyword, 1024, 5120,
                            List.of("//table[.//th//b[contains(text(),'人员档案')]]"),
                            List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                            List.of("//table[.//th//b[contains(.,'人员档案')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    );

                    case "密录" -> webScreenCapturer.capture(
                            "https://prts.wiki/w/" + keyword, 1024, 5120,
                            List.of("//table[.//th//b[contains(text(),'干员密录')]]"),
                            List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                            List.of("//table[.//th//b[contains(.,'干员密录')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    );

                    case "悖论" -> webScreenCapturer.capture(
                            "https://prts.wiki/w/" + keyword, 1024, 5120,
                            List.of("//table[.//th//b[contains(text(),'悖论模拟')]]"),
                            List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                            List.of("//table[.//th//b[contains(.,'悖论模拟')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    );

                    default ->  throw new NullBotException("[PRTS] ❌无此操作");
                };
            } else {
                keyword = String.join(" ", params.subList(0, params.size()));
                base64 = webScreenCapturer.capture(
                        "https://prts.wiki/w/" + keyword, 1024, 5120,
                        List.of("#bodyContent"),
                        List.of(
                                ".backToTop", "#toc", "#rightToc",
                                ".music-btn", "#calc", "#equip-selector",
                                "#干员模型", "#敌人模型", "#spine-root",
                                "#注释与链接", "#catlinks"
                        ),
                        List.of(
                                // "input[onchange*='switchDisplay第一天赋算法']",
                                "input[onchange*='switchDisplay第一天赋潜能']",
                                // "input[onchange*='switchDisplay第二天赋算法']",
                                "input[onchange*='switchDisplay第二天赋潜能']"
                        )
                );
            }

        } catch (NullBotException e) {
            throw e;
        } catch (Exception e) {
            throw new NullBotException("[PRTS] ❌查询失败: " + e.getMessage());
        }

        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("├─[PRTS] 已查询 - {}", keyword);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PRTS 命令
                功能: 明日方舟PRTS查询
                限权: %d 级
                格式:
                1. PRTS [查询内容]
                2. PRTS [可选: 条目] [干员名]
                条目: 语音/档案/密录/悖论
                别名: PRTS查询/prts""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ PRTS 命令
                功能: 通过PRTS网站查询明日方舟相关信息
                格式: PRTS [查询内容]
                示例: PRTS 莱伊""";
    }
}
