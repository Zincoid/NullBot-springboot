package com.zincoid.nullbot.core.module.game.handler;

import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.module.game.logic.LootingGameLogic;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.state.LootingGameState;
import com.zincoid.nullbot.core.module.game.model.LootingPlayer;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LootingMatchHandler extends GameMatchHandler<LootingGameState, LootingGameLogic> {

    private static final String CMD_MOVE = "移动";
    private static final String CMD_SCOUT = "侦察";
    private static final String CMD_LOOT = "搜刮";
    private static final String CMD_ATTACK_AI = "攻击AI";
    private static final String CMD_ATTACK_PLAYER = "攻击玩家";
    private static final String CMD_EVAC = "撤离";

    private final InventoryService inventoryService;
    private final UserService userService;

    public LootingMatchHandler(
            BotOperator botOperator,
            MatchManager matchManager,
            PlayerManager playerManager,
            InventoryService inventoryService,
            UserService userService,
            LootingGameLogic gameLogic
    ) {
        super(botOperator, matchManager, playerManager, gameLogic);
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @Override
    public String gameType() {
        return "摸金";
    }

    @Override
    public void onMatchEnd(Match match) {
        LootingGameState state = games.get(match.getMatchId());
        // 摸金 奖励逻辑
        if (state != null) {
            for (LootingPlayer p : state.getPlayers().values()) {
                if (p.isEvacuated()) {
                    userService.plusExperience(p.getUserId(), 200);
                    for (ItemPO item : p.getBackpack())
                        inventoryService.add(p.getUserId(), item.getId(), 1);
                }
            }
        }
        super.onMatchEnd(match);
    }

    /**
     * 玩家行为 (用户调用)
     */
    public GameResult action(Long userId, String command) {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) return getErrorResult("❌对局不存在");
        LootingGameState state = games.get(match.getMatchId());
        if (state == null) return getErrorResult("❌状态不存在");
        if (state.isFinished()) return GameResult.error("❌对局已结束");

        LootingPlayer p = state.getPlayers().get(userId);
        if (!p.isAlive()) return getSuccessResult(userId, match, true, "💀你已死亡 无法继续行动", "");
        if (p.isEvacuated()) return getSuccessResult(userId, match, true, "🚪你已撤离 无法继续行动", "");

        // 执行动作
        String actionOutput = executeAction(state, p, command);
        if (actionOutput == null) return getErrorResult("❌指令不存在");

        // 推进游戏刻（侦察不消耗刻）
        String selfTick = "";
        String opponentTick = "";
        if (!command.equals(CMD_SCOUT)) {
            List<String> tickOutput = gameLogic.tick(state, userId);
            selfTick = tickOutput.get(0);
            opponentTick = tickOutput.get(1);
        }

        String enemyInfo = gameLogic.checkEnemies(state, p);

        // 构建输出
        String statusLine = "❤️HP: " + p.getHp() + "\n[=== 🕒距迷失还剩 " + (25 - state.getTick()) + " 刻 ===]";
        String availableActions = nextActions(state, p);

        String selfInfo = statusLine + "\n" + actionOutput + selfTick + enemyInfo + "\n" + availableActions;
        String opponentInfo = opponentTick;

        // 游戏结束（撤离 / 迷失 / 死亡）
        if (state.isFinished()) {
            if (state.getTick() > 25) return getFinishResult(userId, match, true, "⏹时间已耗尽！", "⏹时间已耗尽！");
            return getFinishResult(userId, match, true, selfInfo + "\n⏹所有玩家均撤离或死亡", "⏹所有玩家均撤离或死亡");
        }
        return getSuccessResult(userId, match, true, selfInfo, opponentInfo);
    }

    // ================== 工具方法 ==================

    private String executeAction(LootingGameState state, LootingPlayer p, String command) {
        if (command.startsWith(CMD_MOVE)) return gameLogic.move(state, p, command.substring(2).trim());
        if (command.equals(CMD_SCOUT)) return gameLogic.view(state, p);
        if (command.equals(CMD_LOOT)) return gameLogic.loot(state, p);
        if (command.equals(CMD_ATTACK_AI)) return gameLogic.attackAi(state, p);
        if (command.equals(CMD_ATTACK_PLAYER)) {
            List<String> output = gameLogic.attackPlayer(state, p);
            return output.getFirst();
        }
        if (command.equals(CMD_EVAC)) return gameLogic.evac(state, p);
        return null;
    }

    private String nextActions(LootingGameState s, LootingPlayer p) {
        if (s.isFinished()) return "";
        StringBuilder sb = new StringBuilder("[可执行操作(格式:/摸金 [动作])]");
        sb.append("\n侦察 - 移动 [地点] - 搜刮");
        if (s.getMap().node(p.getLocation()).isEvac())
            sb.append(" - 撤离");
        if(!gameLogic.checkEnemies(s, p).isEmpty())
            sb.append("\n⚔️ ====== 攻击AI - 攻击玩家");
        return sb.toString();
    }

    @Override
    protected String render(LootingGameState state) {
        // 仅用于初始化或通用展示
        return state.getMap().printSpawn() + "\n\nTips: 可以先通过\"/摸金\"来侦察一下, 侦察不消耗Tick";
    }
}
