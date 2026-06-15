package com.zincoid.nullbot.core.module.game;

import com.zincoid.nullbot.core.module.game.handler.GameMatchHandler;
import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.PoolManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.model.result.MatchResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Matcher {

    private final PlayerManager playerManager;
    private final MatchManager matchManager;
    private final PoolManager poolManager;
    private final HandlerRegistry handlerRegistry;

    public MatchResult join(Long userId, Long groupId, String userName, String gameType) {
        Player self = playerManager.set(userId, groupId, userName);
        if (self.getStatus() != Player.PlayerStatus.IDLE)
            return MatchResult.fail("已在匹配或游戏中", groupId);
        GameMatchHandler<?, ?> handler = handlerRegistry.get(gameType);
        if (handler == null)
            return MatchResult.fail("不支持该游戏类型", groupId);
        Player other = poolManager.poll(gameType);
        if (other == null) {
            poolManager.add(userId, gameType);
            playerManager.update(userId, Player.PlayerStatus.WAITING);
            return MatchResult.fail("已加入匹配队列...", groupId);
        }
        Match match = matchManager.create(other.getId(), userId, gameType);
        self.setInProgressMatchId(match.getMatchId());
        other.setInProgressMatchId(match.getMatchId());
        handler.onMatchStart(match);
        String message = """
                ✅%s对局匹配成功
                - 玩家1: %s
                - 玩家2: %s
                - MatchID: %s"""
                .formatted(gameType, self.getName(), other.getName(), match.getMatchId());
        return MatchResult.success(message, groupId, other.getInProgressGroupId());
    }

    public MatchResult cancel(Long userId, Long groupId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return MatchResult.fail("玩家暂未注册", groupId);
        if (player.getStatus() != Player.PlayerStatus.WAITING)
            return MatchResult.fail("非匹配中状态", groupId);
        if (!poolManager.remove(userId))
            return MatchResult.fail("不在匹配队列", groupId);
        playerManager.update(userId, Player.PlayerStatus.IDLE);
        return MatchResult.fail("取消匹配成功", groupId);
    }

    public MatchResult finish(Long userId, Long groupId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return MatchResult.fail("玩家暂未注册", groupId);
        String matchId = player.getInProgressMatchId();
        if (matchId == null)
            return MatchResult.fail("玩家未在游戏", groupId);
        Match match = matchManager.get(matchId);
        if (match == null)
            return MatchResult.fail("对局信息错误", groupId);
        GameMatchHandler<?, ?> handler = handlerRegistry.get(match.getGameType());
        if (handler == null)
            return MatchResult.fail("游戏类型错误", groupId);
        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();
        handler.onMatchEnd(match);
        String message = """
                ⚠️%s对局强制终止
                - 玩家1: %s
                - 玩家2: %s
                - MatchID: %s"""
                .formatted(match.getGameType(), p1.getId(), p2.getId(), matchId);
        return MatchResult.success(message, p1.getInProgressGroupId(), p2.getInProgressGroupId());
    }
}
