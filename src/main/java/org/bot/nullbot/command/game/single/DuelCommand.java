package org.bot.nullbot.command.game.single;

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

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        if (duelStorage.getDuel(groupId) != null)
            throw new NullBotMsgException("[斗蛐蛐] ⚠️已在游戏中");

        DuelInfo duel = duelStorage.initDuel(groupId);
        bot.sendGroupMsg(groupId, """
                [斗蛐蛐] 测试:录入30秒(L/R)
                %s""".formatted(duel), false);

        List<Pair<Long, String>> inputs = botNextInputer.request(BniMode.GM, groupId, 30, "[LlRr]");

        Map<Long, Pair<Long, String>> lastInputMap = new LinkedHashMap<>();
        for (Pair<Long, String> input : inputs) lastInputMap.put(input.getKey(), input);
        inputs = new ArrayList<>(lastInputMap.values());

        List<Long> left = new ArrayList<>();
        List<Long> right = new ArrayList<>();
        for (Pair<Long, String> pair : inputs) {
            if (pair.getRight().equalsIgnoreCase("L")) {
                left.add(pair.getLeft());
            } else if (pair.getRight().equalsIgnoreCase("R")) {
                right.add(pair.getLeft());
            }
        }

        bot.sendGroupMsg(groupId, """
                [斗蛐蛐] 测试:判断结果
                - %s侧胜出
                - Winner: %s
                - Loser: %s"""
                .formatted(
                        duel.getWinner(),
                        left.stream().map(u -> bot.getStrangerInfo(u, true).getData().getNickname()).toList(),
                        right.stream().map(u -> bot.getStrangerInfo(u, true).getData().getNickname()).toList()
                ), false
        );
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Duel 命令
                功能:
                限权: %d 级
                格式:
                别名: 斗蛐蛐""", getAccess()
        );
    }
}
