package com.zincoid.nullbot.core.module.game.impl.looting;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.framework.GameHandler;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.GameRes;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.game.impl.looting.model.LootingPlayer;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.service.base.InventoryService;
import com.zincoid.nullbot.core.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class LootingHandler extends GameHandler<LootingState, LootingLogic, LootingRenderer> {

    private static final String CMD_MOVE = "移动";
    private static final String CMD_SCOUT = "侦察";
    private static final String CMD_LOOT = "搜刮";
    private static final String CMD_ATTACK_AI = "攻击AI";
    private static final String CMD_ATTACK_PLAYER = "攻击玩家";
    private static final String CMD_EVAC = "撤离";

    private final InventoryService inventoryService;
    private final UserService userService;

    public LootingHandler(
            BotOperator botOperator,
            MatchManager matchManager,
            PlayerManager playerManager,
            InventoryService inventoryService,
            UserService userService,
            LootingLogic gameLogic,
            LootingRenderer renderer
    ) {
        super(botOperator, matchManager, playerManager, gameLogic, renderer);
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @Override
    public String getType() {
        return "摸金";
    }

    @Override
    public String getPattern() {
        return "^(移动|侦察|搜刮|攻击AI|攻击玩家|撤离).*";
    }

    @Override
    public void onStart(Match match, LootingState state) {
        Player p1 = match.getP1();
        Player p2 = match.getP2();
        String message = renderer.render(state);
        if (!Objects.equals(p1.getInProgressGroupId(), p2.getInProgressGroupId()))
            botOperator.sendGroupMsg(p1.getInProgressGroupId(), message);
        botOperator.sendGroupMsg(p2.getInProgressGroupId(), message);
    }

    @Override
    public void onEnd(Match match, LootingState state) {
        state.getPlayers().values().forEach(p -> {
            if (p.isEvacuated()) {
                userService.plusExperience(p.getUserId(), 200);
                for (ItemPO item : p.getBackpack())
                    inventoryService.add(p.getUserId(), item.getId(), 1);
            }
        });
    }

    @Override
    public GameRes onAction(LootingState state, Player self, Player opp, CmdArgs args) {
        String command = args.nextFullString("侦察");

        if (state.isFinished()) return fail("对局已结束");

        LootingPlayer p = state.getPlayers().get(self.getId());
        if (!p.isAlive()) return success(true, "💀你已死亡 无法继续行动", "");
        if (p.isEvacuated()) return success(true, "🚪你已撤离 无法继续行动", "");

        log.info("☑ [Looting] 玩家 {} 执行指令 [{}]", self.getId(), command);

        String actionOutput;
        String opponentExtra = "";
        if (command.equals(CMD_ATTACK_PLAYER)) {
            List<String> output = gameLogic.attackPlayer(state, p);
            actionOutput = output.get(0);
            opponentExtra = output.get(1);
        } else if (command.startsWith(CMD_MOVE)) {
            actionOutput = gameLogic.move(state, p, command.substring(2).trim());
        } else if (command.equals(CMD_SCOUT)) {
            actionOutput = gameLogic.view(state, p);
        } else if (command.equals(CMD_LOOT)) {
            actionOutput = gameLogic.loot(state, p);
        } else if (command.equals(CMD_ATTACK_AI)) {
            actionOutput = gameLogic.attackAi(state, p);
        } else if (command.equals(CMD_EVAC)) {
            actionOutput = gameLogic.evac(state, p);
        } else {
            return fail("指令不存在");
        }

        String selfTick = "";
        String opponentTick = "";
        if (!command.equals(CMD_SCOUT)) {
            List<String> tickOutput = gameLogic.tick(state, self.getId());
            selfTick = tickOutput.get(0);
            opponentTick = tickOutput.get(1);
        }

        String enemyInfo = gameLogic.checkEnemies(state, p);
        String statusLine = renderer.statusLine(p, 25 - state.getTick());
        String menu = renderer.actionMenu(state.isFinished(),
                state.getMap().node(p.getLocation()).isEvac(), !enemyInfo.isEmpty());
        String selfMessage = statusLine + "\n" + actionOutput + selfTick + enemyInfo + "\n" + menu;
        String oppMessage = opponentExtra.isEmpty() ? opponentTick
                : opponentTick.isEmpty() ? opponentExtra : opponentExtra + "\n" + opponentTick;

        if (state.isFinished()) {
            if (state.getTick() > 25) return finish(true, "⏹时间已耗尽！", "⏹时间已耗尽！");
            return finish(true, selfMessage + "\n⏹所有玩家均撤离或死亡", "⏹所有玩家均撤离或死亡");
        }
        return success(true, selfMessage, oppMessage);
    }

}
