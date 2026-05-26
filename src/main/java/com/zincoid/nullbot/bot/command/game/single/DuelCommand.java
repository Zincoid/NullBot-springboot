package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.core.component.storage.DuelStorage;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.DuelInfo;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.util.Base64Util;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CommandMapping({"Duel", "斗蛐蛐"})
@Component
@Slf4j
@RequiredArgsConstructor
public class DuelCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final DuelStorage duelStorage;
    private final BotInputManager botInputManager;

    private static final int SELECTION_TIME = 30;  // 抉择时间 (单位: Second)

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        if (duelStorage.getDuel(groupId) != null)
            throw new NullBotException("[斗蛐蛐] ⚠️已在游戏中");

        try {
            DuelInfo duel = duelStorage.initDuel(groupId);

            MsgUtils builder = MsgUtils.builder().text("[斗蛐蛐] ⚔️请交战双方无序入场");
            builder.text("\n[ ====== 左方选手 ====== ]\n");
            for (Map.Entry<Integer, Integer> enemy : duel.getLeft().entrySet())
                builder.img("base64://" + Base64Util.from(getIconPath(enemy.getKey())))
                        .text("*" + enemy.getValue() + " ");
            builder.text("\n[ ====== 右方选手 ====== ]\n");
            for (Map.Entry<Integer, Integer> enemy : duel.getRight().entrySet())
                builder.img("base64://" + Base64Util.from(getIconPath(enemy.getKey())))
                        .text("*" + enemy.getValue() + " ");
            builder.text("\n\n注: 发送L或R进行选择(%s秒内)".formatted(SELECTION_TIME));
            bot.sendGroupMsg(groupId, builder.build(), false);

            List<Pair<Long, String>> inputs = botInputManager
                    .request(BniMode.GM, groupId, "[LlRr]", SELECTION_TIME);

            Map<Long, Pair<Long, String>> lastInputMap = new LinkedHashMap<>();
            for (Pair<Long, String> input : inputs) lastInputMap.put(input.getKey(), input);
            inputs = new ArrayList<>(lastInputMap.values());

            List<Long> left = new ArrayList<>();
            List<Long> right = new ArrayList<>();
            for (Pair<Long, String> pair : inputs)
                if (pair.getRight().equalsIgnoreCase("L"))
                    left.add(pair.getLeft());
                else if (pair.getRight().equalsIgnoreCase("R"))
                    right.add(pair.getLeft());

            List<Long> winners;
            List<Long> losers;
            if ("L".equals(duel.getWinner())) {
                winners = left;
                losers = right;
            } else if ("R".equals(duel.getWinner())) {
                winners = right;
                losers = left;
            } else
                throw new NullBotException("[斗蛐蛐] ❌数据异常");

            List<String> winnerNames = winners.stream()
                    .map(u -> bot.getStrangerInfo(u, true).getData().getNickname())
                    .toList();
            List<String> loserNames = losers.stream()
                    .map(u -> bot.getStrangerInfo(u, true).getData().getNickname())
                    .toList();

            bot.sendGroupMsg(groupId, """
                    [斗蛐蛐] ⌛️计时结束 - %s侧胜出
                    [ ======= 赢家 ======= ]
                    %s
                    [ ======= 输家 ======= ]
                    %s""".formatted(duel.getWinner(),
                    winnerNames.isEmpty() ? "无" : String.join(", ", winnerNames),
                    loserNames.isEmpty() ? "无" : String.join(", ", loserNames)), false
            );

        } catch (Exception e) {
            throw new NullBotException("[斗蛐蛐] ❌" + e.getMessage());
        } finally {
            duelStorage.removeDuel(groupId);
        }
    }

    private String getIconPath(int id) {
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
