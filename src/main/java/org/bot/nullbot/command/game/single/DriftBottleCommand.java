package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.DriftBottlePO;
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

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        if (params.isEmpty())
            throw new NullBotMsgException("[漂流瓶] ❌无操作");
        String option = params.getFirst();

        if ("扔".equals(option)) {
            if (params.size() < 2)
                throw new NullBotMsgException("[漂流瓶] ❌无文本");
            String text = String.join(" ", params.subList(1, params.size()));
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            int thrown = driftBottleService.throwBottle(userId, userName, text);
            bot.sendGroupMsg(event.getGroupId(), thrown == 1 ? "✉️ 已投出！" : "[漂流瓶] ❌出错", false);
            log.info("\t\t\t\t├─[DriftBottle] 扔漂流瓶 - {} -> {}", userId, thrown == 1 ? "已投出" : "出错");
            return;
        }

        if ("捡".equals(option)) {
            DriftBottlePO bottle = driftBottleService.pickUpRand();
            bot.sendGroupMsg(groupId, bottle.toString(), false);
            log.info("\t\t\t\t├─[DriftBottle] 捡漂流瓶 - {} -> #{}", userId, bottle.getId());
            return;
        }

        throw new NullBotMsgException("[漂流瓶] ❌非法操作");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DriftBottle 命令
                功能: 扔或者捡一个漂流瓶
                限权: %d 级
                格式:
                1. DriftBottle [扔] [文本]
                2. DriftBottle [捡]
                别名: 漂流瓶""", getAccess()
        );
    }
}
