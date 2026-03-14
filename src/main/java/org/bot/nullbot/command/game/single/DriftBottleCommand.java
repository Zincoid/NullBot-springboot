package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.entity.po.DriftBottlePO;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.DriftBottleService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"DriftBottle", "漂流瓶"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DriftBottleCommand implements Command
{
    private final DriftBottleService driftBottleService;
    private final BotNextInputer botNextInputer;

    private static final int KEEP_TIME = 30;  // 漂流瓶保留时间

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String message = event.getMessage();
        if (message.contains(" ")) {
            int thrown = driftBottleService.throwBottle(
                    userId,
                    bot.getStrangerInfo(userId, true).getData().getNickname(),
                    message.substring(message.indexOf(" "))
            );
            bot.sendGroupMsg(event.getGroupId(),
                    thrown == 1 ? "✉️ 已投出！" : "[漂流瓶] ❌出错", false);
            log.info("\t\t\t\t├─[DriftBottle] 扔漂流瓶 - {} -> {}",
                    userId, thrown == 1 ? "已投出" : "出错");
        } else {
            DriftBottlePO bottle = driftBottleService.pickUpRand();
            if (bottle == null)
                throw new NullBotMsgException("没有漂流瓶了！");
            bot.sendGroupMsg(groupId, bottle.toString(), false);
            List<Pair<Long, String>> inputs;
            try {
                inputs = botNextInputer
                        .request(BniMode.PS, userId, "扔回去", KEEP_TIME, true);
            } catch (Exception e) {
                throw new NullBotMsgException("[漂流瓶] ❌" + e.getMessage());
            }
            if (!inputs.isEmpty()) {
                int thrown = driftBottleService.throwBottle(bottle);
                bot.sendGroupMsg(groupId,
                        thrown == 1 ? "✉️ 已投回！" : "[漂流瓶] ❌出错", true);
            }
            log.info("\t\t\t\t├─[DriftBottle] {} - {} -> #{}",
                    inputs.isEmpty() ? "捡漂流瓶" : "捡漂流瓶并投回",
                    userId, bottle.getId());
        }
    }

    public static int getKeepTime() { return KEEP_TIME; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DriftBottle 命令
                功能: 扔或者捡一个漂流瓶
                限权: %d 级
                格式: DriftBottle [可选: 文本]
                别名: 漂流瓶
                注意: 可发送"扔回去"投回""", getAccess()
        );
    }
}
