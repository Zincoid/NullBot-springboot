package com.zincoid.nullbot.bot.command.game.engine.dual;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.model.Result;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.impl.looting.LootingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
public class LootingCmd implements Cmd {

    private final LootingHandler lootingHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Result result = lootingHandler.act(event.getUserId(), args);
        result.send();
        log.info("☑ [Looting] 摸金操作 -> {}", result.isOk());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Looting 命令
                功能: 双人 PvPvE 摸金对抗
                限权: %s 级
                奖励: 所有带出物品 & 200Exp
                格式: Looting [可选: 指令]
                
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
                - 25 刻后未撤离则迷失并掉落全部物品
                别名: 摸金""", getAccess()
        );
    }
}

