package org.bot.nullbot.command.game.looting;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.LootingMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.springframework.stereotype.Component;


@CommandMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LootingCommand implements Command
{
    private final LootingMatchHandler lootingMatchHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event)
    {
        if (!(event.getEvent() instanceof GroupMessageEvent groupMessageEvent)) {
            log.info("\t\t\t\t├─[Looting] 非群消息，忽略");
            return;
        }

        Long groupId = groupMessageEvent.getGroupId();
        Long userId = groupMessageEvent.getUserId();

        // 无参数 = 侦察
        String commandText = event.getCommandParameters().isEmpty()
                ? "侦察"
                : String.join(" ", event.getCommandParameters());

        GameResult result = lootingMatchHandler.action(userId, commandText);

        // 当前群输出（始终）
        bot.sendGroupMsg(groupId, result.getInfo(), false);

        // 跨群同步（同一 tick 世界变化）
        if (result.getSuccess() && !result.getIsSameGroup()) {
            bot.sendGroupMsg(
                    result.getOpponentGroupId(),
                    result.getInfo(),
                    false
            );
        }

        log.info(
                "\t\t\t\t├─[Looting] 玩家 {} 执行指令 [{}]",
                userId,
                commandText
        );
    }

    @Override
    public String getHelp()
    {
        return """
                ◉ Looting（摸金行动）
                功能：双人 PVPVE 非回合制摸金对抗
                限权：%s

                基础指令：
                Looting 查看
                Looting 移动 [地点]
                Looting 搜刮
                Looting 攻击AI
                Looting 攻击玩家
                Looting 撤离

                说明：
                - 任意玩家行动都会推进游戏刻
                - AI 会在 Tick 中移动 / 攻击
                - 超过 50 Tick 全员迷失，掉落全部物品
                """.formatted(getAccess());
    }
}

