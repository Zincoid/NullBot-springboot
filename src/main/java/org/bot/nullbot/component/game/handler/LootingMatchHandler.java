package org.bot.nullbot.component.game.handler;

import com.mikuac.shiro.core.BotContainer;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.component.game.PlayerManager;
import org.bot.nullbot.component.game.logic.LootingGameLogic;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.looting.LootingGameState;
import org.bot.nullbot.entity.game.looting.LootingPlayerState;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.service.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LootingMatchHandler extends GameMatchHandler<LootingGameState, LootingGameLogic>
{
    private final InventoryService inventoryService;

    public LootingMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            InventoryService inventoryService,
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
        for(LootingPlayerState p : state.getPlayers().values()){
            if(p.isEvacuated())
                for(ItemPO item : p.getBackpack())
                    inventoryService.increaseInventory(p.getUserId(), item);
        }
        super.onMatchEnd(match);
    }

    /**
     * 玩家行为入口 (由 Command 调用)
     */
    public GameResult action(Long userId, String command)
    {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) { return GameResult.error("[Looting] ❌ 你当前没有进行中的对局"); }
        LootingGameState state = games.get(match.getMatchId());
        if (state == null) { return GameResult.error("[Looting] ❌ 游戏状态不存在"); }
        if (state.isFinished()) { return GameResult.error("[Looting] ❌ 对局已结束"); }

        LootingPlayerState p = state.getPlayers().get(userId);
        if (!p.isAlive()) return GameResult.success(null, "💀 你已死亡，无法继续行动");
        if (p.isEvacuated()) return GameResult.success(null, "🚪 你已撤离，无法继续行动");

        matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        Long opponentGroupId = match.getOpponentGroupIdBySelfId(userId);
        StringBuilder result = new StringBuilder();
        if (command.startsWith("移动")) result.append(gameLogic.move(state, p, command.substring(2).trim())).append("\n").append(gameLogic.tick(state, userId, opponentGroupId)).append(gameLogic.checkEnemies(state, p));
        else if (command.equals("侦察")) result.append(gameLogic.view(state, p)).append("\n").append(gameLogic.checkEnemies(state, p));
        else if (command.equals("搜刮")) result.append(gameLogic.loot(state, p)).append("\n").append(gameLogic.tick(state, userId, opponentGroupId)).append(gameLogic.checkEnemies(state, p));
        else if (command.equals("攻击AI")) result.append(gameLogic.attackAi(state, p)).append("\n").append(gameLogic.tick(state, userId, opponentGroupId)).append(gameLogic.checkEnemies(state, p));
        else if (command.equals("攻击玩家")) result.append(gameLogic.attackPlayer(state, p, opponentGroupId)).append("\n").append(gameLogic.tick(state, userId, opponentGroupId)).append(gameLogic.checkEnemies(state, p));
        else if (command.equals("撤离")) result.append(gameLogic.evac(state, p)).append("\n").append(gameLogic.tick(state, userId, opponentGroupId)).append(gameLogic.checkEnemies(state, p));
        else {
            result.append("❓ 未知指令");
            return GameResult.success(null, result.toString());
        }

        String info = "[玩家" + userId + "] HP: " + state.getPlayers().get(userId).getHp() + "\n" + result.append(nextActions(state, p));

        // 游戏结束（撤离 / 迷失 / 死亡）
        if (state.isFinished()) {
            if (state.getTick() > 25) {
                return getFinishResult( userId, match, info + "\n\n⏹ 时间已耗尽！(10 Ticks)");
            }
            return getFinishResult( userId, match, info + "\n\n⏹ 对局已结束！");
        }
        return GameResult.success(null, info);
    }

    public String nextActions(LootingGameState s, LootingPlayerState p) {
        if (s.isFinished()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\n\n[可执行操作]");
        sb.append("\n侦察 / 移动 [地点] / 搜刮");
        if(!gameLogic.checkEnemies(s, p).isEmpty())
            sb.append("\n攻击AI / 攻击玩家");
        if (s.getMap().node(p.getLocation()).isEvac()) {
            sb.append(" / 撤离");
        }
        return sb.toString();
    }

    @Override
    protected String render(LootingGameState state)
    {
        // 仅用于初始化或通用展示
        return state.getMap().printSpawn() + "\n\nTips: 可以先通过\"/摸金 [命令]\"来侦察一下, 侦察不消耗Tick";
    }
}
