package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.handler.LootingMatchHandler;
import com.zincoid.nullbot.core.model.result.GameResult;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
public class LootingCmd implements Cmd {

    private final LootingMatchHandler lootingMatchHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long userId = event.getUserId();
        String commandText = args.nextFullString("侦察");
        log.info("☑ [Looting] 玩家 {} 执行指令 [{}]", userId, commandText);

        GameResult result = lootingMatchHandler.action(userId, commandText);
        if (!result.getSuccess()) {
            bot.sendGroupMsg(event.getGroupId(), result.getSelfInfo(), false);
            return;
        }
        if (!result.getIsAsync()) throw new BotWarnException("游戏不支持同步消息");
        if (!result.getSelfInfo().isEmpty())
            bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
        if (!result.getOpponentInfo().isEmpty())
            bot.sendGroupMsg(result.getOpponentGroupId(), result.getOpponentInfo(), false);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Looting 命令
                功能: 双人 PvPvE 非回合制摸金对抗
                奖励: 所有带出物品 & 200Exp
                限权: %s 级
                格式: Looting [可选: 指令]
                别名: 摸金
                
                指令:
                - 侦察
                - 移动 [地点]
                - 搜刮
                - 攻击AI
                - 攻击玩家
                - 撤离
                
                说明:
                - 任意玩家行动(除侦察)会推进游戏刻
                - AI 会在刻中移动或攻击
                - 25 刻后未撤离则迷失并掉落全部物品""", getAccess()
        );
    }
}

