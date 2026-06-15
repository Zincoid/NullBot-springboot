package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.impl.looting.LootingHandler;
import org.springframework.stereotype.Component;

@CmdMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
public class LootingCmd implements Cmd {

    private final LootingHandler lootingHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        lootingHandler.act(event.getUserId(), args).send();
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

