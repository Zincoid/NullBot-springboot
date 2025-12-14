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
import org.bot.nullbot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LootingMatchHandler extends GameMatchHandler<LootingGameState, LootingGameLogic>
{
    public LootingMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            UserService userService,
            LootingGameLogic gameLogic
    ) {
        super(
                botId,
                botContainer,
                matchManager,
                playerManager,
                userService,
                gameLogic,
                new ConcurrentHashMap<>()
        );
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
        if (command.startsWith("移动")) result.append(gameLogic.move(state, p, command.substring(2).trim())).append(gameLogic.tick(state, userId, opponentGroupId));
        else if (command.equals("侦察")) {
            result.append(gameLogic.view(state, p));
            gameLogic.checkFinished(state);
        }
        else if (command.equals("搜刮")) result.append(gameLogic.loot(state, p)).append(gameLogic.tick(state, userId, opponentGroupId));
        else if (command.equals("攻击AI")) result.append(gameLogic.attackAi(state, p)).append(gameLogic.tick(state, userId, opponentGroupId));
        else if (command.equals("攻击玩家")) result.append(gameLogic.attackPlayer(state, p, opponentGroupId)).append(gameLogic.tick(state, userId, opponentGroupId));
        else if (command.equals("撤离")) result.append(gameLogic.evac(state, p)).append(gameLogic.tick(state, userId, opponentGroupId));
        else result.append("❓ 未知指令");

        String info = "玩家" + userId + " HP: " + Math.max(state.getPlayers().get(userId).getHp(), 0)
                + "\n" + result.append("\n").append(nextActions(state, p));

        // 游戏结束（撤离 / 迷失 / 死亡）
        if (state.isFinished()) {
            if (state.getTick() > 10) {
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
        StringBuilder sb = new StringBuilder("\n[可执行操作]");
        sb.append("\n侦察 / 移动 [地点] / 搜刮");
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
