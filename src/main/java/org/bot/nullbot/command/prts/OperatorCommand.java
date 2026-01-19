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

@CommandMapping({"Operator", "PRTS", "prts", "干员查询", "干员"})
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

            String operator;
            String base64;
            try {
                if (params.size() > 1) {
                    // 操作方法
                    String option = params.get(0);
                    operator = params.get(1);
                    base64 = switch (option) {
                        case "档案" -> webScreenCapturer.capture(
                                    "https://prts.wiki/w/" + operator, 1024, 5120,
                                    List.of("table.wikitable.mw-collapsible.logo.mw-made-collapsible"),
                                    List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                                    List.of("button[class*='mw-collapsible-toggle']")
                            );
                        default -> throw new NullBotMsgException("[干员查询] ❌无此操作");
                    };
                }else{
                    // 默认方法
                    operator = params.getFirst();
                    base64 = webScreenCapturer.capture(
                            "https://prts.wiki/w/" + operator, 1024, 5120,
                            List.of("#bodyContent"),
                            List.of(
                                    ".backToTop", "#toc", "#rightToc",
                                    ".music-btn", "#calc", "#equip-selector",
                                    "#干员模型", "#spine-root",
                                    "#注释与链接", "#catlinks"
                            ),
                            List.of(
                                    "input[onchange*='switchDisplay第一天赋算法']",
                                    "input[onchange*='switchDisplay第一天赋潜能']",
                                    "input[onchange*='switchDisplay第二天赋算法']",
                                    "input[onchange*='switchDisplay第二天赋潜能']"
                            )
                    );
                }
            } catch (NullBotMsgException e) {
                throw e;
            } catch (Exception e) {
                throw new NullBotMsgException("[干员查询] ❌查询失败: " + e.getMessage());
            }

            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Operator] 已查询 - {}", operator);
        }else
            throw new NullBotLogException("[干员查询] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Operator 命令
                功能: 明日方舟PRTS干员查询
                限权: %d 级
                格式: Operator [可选: 查询内容] [干员名]
                查询内容: 档案/...
                别名: PRTS/prts/干员查询/干员""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Operator 命令
                功能: 通过PRTS网站查询明日方舟干员信息
                限权: %d 级
                格式: Operator [干员名]
                示例: Operator 莱伊""", getAccess()
        );
    }
}
