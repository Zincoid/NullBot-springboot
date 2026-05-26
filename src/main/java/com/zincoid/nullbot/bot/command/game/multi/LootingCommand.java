package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.game.handler.impl.LootingMatchHandler;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.bot.exception.NullBotException;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
public class LootingCommand implements Command {

    private final LootingMatchHandler lootingMatchHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        Long userId = event.getUserId();
        String commandText = params.nextFullString("侦察");
        GameResult result = lootingMatchHandler.action(userId, commandText);
        if (result.getSuccess()) {
            if (!result.getIsAsync()) throw new NullBotException("该模式不发送同步消息");
            if (!result.getSelfInfo().isEmpty())
                bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
            if (!result.getOpponentInfo().isEmpty())
                bot.sendGroupMsg(result.getOpponentGroupId(), result.getOpponentInfo(), false);
        } else bot.sendGroupMsg(event.getGroupId(), result.getSelfInfo(), false);
        log.info("☑ [Looting] 玩家 {} 执行指令 [{}]", userId, commandText);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Looting 命令
                功能: 双人 PVPVE 非回合制摸金对抗
                奖励: 所有带出物品 & 200Exp
                限权: %s 级
                别名: 摸金
                
                基础指令:
                - Looting 侦察
                - Looting 移动 [地点]
                - Looting 搜刮
                - Looting 攻击AI
                - Looting 攻击玩家
                - Looting 撤离
                
                说明:
                - 任意玩家行动(侦察除外)都会推进游戏刻
                - AI 会在 Tick 中移动 / 攻击
                - 25 Tick后未撤离则迷失并掉落全部物品""", getAccess()
        );
    }
}

