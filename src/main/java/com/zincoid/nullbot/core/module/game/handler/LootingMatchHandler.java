package com.zincoid.nullbot.core.module.game.handler;

import com.mikuac.shiro.core.BotContainer;
import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.module.game.logic.LootingGameLogic;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.state.LootingGameState;
import com.zincoid.nullbot.core.module.game.model.LootingPlayer;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LootingMatchHandler extends GameMatchHandler<LootingGameState, LootingGameLogic> {

    private final InventoryService inventoryService;
    private final UserService userService;

    public LootingMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            InventoryService inventoryService,
            UserService userService,
            LootingGameLogic gameLogic
    ) {
        super(
                botId,
                botContainer,
                matchManager,
                playerManager,
                gameLogic,
                new ConcurrentHashMap<>()
        );
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @Override
    public String gameType() {
        return "摸金";
    }

    @Override
    public void onMatchStart(Match match)
    {
        LootingGameState state = gameLogic.create(match);
        games.put(match.getMatchId(), state);
        super.onMatchStart(match);
        sendInitMessage(match, state);
    }

    @Override
    public void onMatchEnd(Match match)
    {
        LootingGameState state = games.get(match.getMatchId());
        // 摸金 奖励逻辑
        for (LootingPlayer p : state.getPlayers().values()) {
            if (p.isEvacuated()) {
                userService.plusExperience(p.getUserId(), 200);
                for (ItemPO item : p.getBackpack())
                    inventoryService.add(p.getUserId(), item.getId(), 1);
            }
        }
        super.onMatchEnd(match);
    }

    /**
     * 玩家行为 (用户调用)
     */
    public GameResult action(Long userId, String command)
    {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) return getErrorResult("❌对局不存在");
        LootingGameState state = games.get(match.getMatchId());
        if (state == null) return getErrorResult("❌状态不存在");
        if (state.isFinished()) return GameResult.error("❌对局已结束");

        LootingPlayer p = state.getPlayers().get(userId);
        if (!p.isAlive()) return getSuccessResult(userId, match, true, "💀你已死亡 无法继续行动", "");
        if (p.isEvacuated()) return getSuccessResult(userId, match, true, "🚪你已撤离 无法继续行动", "");

        StringBuilder selfOutput = new StringBuilder();
        StringBuilder opponentOutput = new StringBuilder();

        if (command.startsWith("移动")) selfOutput.append(gameLogic.move(state, p, command.substring(2).trim()));
        else if (command.equals("侦察")) selfOutput.append(gameLogic.view(state, p));
        else if (command.equals("搜刮")) selfOutput.append(gameLogic.loot(state, p));
        else if (command.equals("攻击AI")) selfOutput.append(gameLogic.attackAi(state, p));
        else if (command.equals("攻击玩家")) {
            List<String> attackPlayerOutput = gameLogic.attackPlayer(state, p);
            selfOutput.append(attackPlayerOutput.get(0));
            opponentOutput.append(attackPlayerOutput.get(1));
        }
        else if (command.equals("撤离")) selfOutput.append(gameLogic.evac(state, p));
        else return getErrorResult("指令不存在");

        selfOutput.append("\n");

        if(!command.equals("侦察")){
            List<String> tickOutput = gameLogic.tick(state, userId);
            selfOutput.append(tickOutput.get(0));
            opponentOutput.append(tickOutput.get(1));
        }

        selfOutput.append(gameLogic.checkEnemies(state, p));

        String selfInfo = "【玩家" + userId + "】 HP: " + state.getPlayers().get(userId).getHp() + "\uD83D\uDC9F"
                + "\n[=== \uD83D\uDD52 距迷失还剩 " + (25 - state.getTick()) + " 刻 ===]"
                + selfOutput.append(nextActions(state, p));
        String opponentInfo = opponentOutput.toString();

        // 游戏结束（撤离 / 迷失 / 死亡）
        if (state.isFinished()) {
            if (state.getTick() > 25) {
                return getFinishResult(userId, match, true, "⏹时间已耗尽！", "⏹时间已耗尽！");
            }
            return getFinishResult(userId, match, true, selfInfo + "\n⏹所有玩家均撤离或死亡", "⏹所有玩家均撤离或死亡");
        }
        return getSuccessResult(userId, match, true, selfInfo, opponentInfo);
    }

    // ================== 工具方法 ==================

    public String nextActions(LootingGameState s, LootingPlayer p) {
        if (s.isFinished()) return "";
        StringBuilder sb = new StringBuilder("\n[可执行操作(格式:/摸金 [动作])]");
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
