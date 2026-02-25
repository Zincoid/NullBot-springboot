package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.component.storage.DuelStorage;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.DuelInfo;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CommandMapping({"Duel", "斗蛐蛐"})
@Component
@Slf4j
@RequiredArgsConstructor
public class DuelCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final DuelStorage duelStorage;
    private final BotNextInputer botNextInputer;

    private static final int SELECTION_TIME = 30;  // 抉择时间 单位: Second

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        if (duelStorage.getDuel(groupId) != null)
            throw new NullBotMsgException("[斗蛐蛐] ⚠️已在游戏中");

        DuelInfo duel = duelStorage.initDuel(groupId);
        MsgUtils builder = MsgUtils.builder().text("[斗蛐蛐] ⚔️请交战双方无序入场\n");
        builder.text("[ ====== 左方选手 ====== ]\n");
        for (Map.Entry<Integer, Integer> enemy : duel.getLeft().entrySet())
            builder.img(icon(enemy.getKey())).text("*" + enemy.getValue() + " ");
        builder.text("\n[ ====== 右方选手 ====== ]\n");
        for (Map.Entry<Integer, Integer> enemy : duel.getRight().entrySet())
            builder.img(icon(enemy.getKey())).text("*" + enemy.getValue() + " ");
        builder.text("\n\n请发送L或R进行选择(%s秒内)".formatted(SELECTION_TIME));
        bot.sendGroupMsg(groupId, builder.build(), false);

        List<Pair<Long, String>> inputs = botNextInputer.request(BniMode.GM, groupId, SELECTION_TIME, "[LlRr]");

        Map<Long, Pair<Long, String>> lastInputMap = new LinkedHashMap<>();
        for (Pair<Long, String> input : inputs) lastInputMap.put(input.getKey(), input);
        inputs = new ArrayList<>(lastInputMap.values());

        List<Long> left = new ArrayList<>();
        List<Long> right = new ArrayList<>();
        for (Pair<Long, String> pair : inputs) {
            if (pair.getRight().equalsIgnoreCase("L"))
                left.add(pair.getLeft());
            else if (pair.getRight().equalsIgnoreCase("R"))
                right.add(pair.getLeft());
        }

        List<Long> winners;
        List<Long> losers;
        if ("L".equals(duel.getWinner())) {
            winners = left;
            losers = right;
        } else if ("R".equals(duel.getWinner())) {
            winners = right;
            losers = left;
        } else
            throw new NullBotMsgException("[斗蛐蛐] ❌数据异常");

        List<String> winnerNames = winners.stream().map(u -> bot.getStrangerInfo(u, true).getData().getNickname()).toList();
        List<String> loserNames = losers.stream().map(u -> bot.getStrangerInfo(u, true).getData().getNickname()).toList();

        bot.sendGroupMsg(groupId, """
                [斗蛐蛐] ⌛️计时结束 - %s侧胜出
                [ ======= 赢家 ======= ]
                %s
                [ ======= 输家 ======= ]
                %s""".formatted(duel.getWinner(),
                String.join(", ", winnerNames),
                String.join(", ", loserNames)), false
        );

        duelStorage.removeDuel(groupId);
    }

    private String icon(int id) {
        return fileStorageProperties.getResourcePath() + "/duel/icon/" + id + ".png";
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Duel 命令
                功能: 明日方舟斗蛐蛐
                限权: %d 级
                格式: Duel
                别名: 斗蛐蛐""", getAccess()
        );
    }
}
