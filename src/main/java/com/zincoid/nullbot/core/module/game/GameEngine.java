package com.zincoid.nullbot.core.module.game;

import com.zincoid.nullbot.core.context.BotCtx;
import com.zincoid.nullbot.core.module.game.runtime.InputOrchestrator;
import com.zincoid.nullbot.core.module.game.runtime.*;
import com.zincoid.nullbot.core.module.game.framework.GameHandler;
import com.zincoid.nullbot.core.model.result.MatchResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEngine {

    private static final long WAITING_TIMEOUT_SECONDS = 120;
    private static final long PLAYING_TIMEOUT_SECONDS = 240;

    private final PlayerManager playerManager;
    private final MatchManager matchManager;
    private final MatchingPool matchingPool;
    private final HandlerRegistry handlerRegistry;
    private final BotOperator botOperator;
    private final InputOrchestrator inputOrchestrator;

    public MatchResult join(Long userId, String userName, String type) {
        Player self = playerManager.set(userId, BotCtx.getGroupId(), userName);
        if (self.getStatus() != Player.PlayerStatus.IDLE)
            return MatchResult.fail("已在匹配或游戏中");
        GameHandler<?, ?, ?> handler = handlerRegistry.get(type);
        if (handler == null)
            return MatchResult.fail("不支持该游戏类型");
        Player opp = matchingPool.poll(type);
        if (opp == null) {
            matchingPool.add(userId, type);
            playerManager.update(userId, Player.PlayerStatus.WAITING);
            return MatchResult.success("已加入匹配队列...");
        }
        Match match = matchManager.create(userId, opp.getId(), type);
        self.setInProgressMatchId(match.getId());
        opp.setInProgressMatchId(match.getId());
        handler.start(match);
        inputOrchestrator.listen(match, handler);
        String message = """
                ✅%s对局匹配成功
                - P1: %s(%s)
                - P2: %s(%s)
                - MatchID: %s"""
                .formatted(type, self.getName(), self.getId(), opp.getName(), opp.getId(), match.getId());
        return MatchResult.success(opp.getInProgressGroupId(), message);
    }

    public MatchResult cancel(Long userId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return MatchResult.fail("玩家暂未注册");
        if (player.getStatus() != Player.PlayerStatus.WAITING)
            return MatchResult.fail("非匹配中状态");
        if (!matchingPool.remove(userId))
            return MatchResult.fail("不在匹配队列");
        playerManager.update(userId, Player.PlayerStatus.IDLE);
        return MatchResult.fail("取消匹配成功");
    }

    public MatchResult finish(Long userId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return MatchResult.fail("玩家暂未注册");
        String matchId = player.getInProgressMatchId();
        if (matchId == null)
            return MatchResult.fail("玩家未在游戏");
        Match match = matchManager.get(matchId);
        if (match == null)
            return MatchResult.fail("对局信息错误");
        GameHandler<?, ?, ?> handler = handlerRegistry.get(match.getType());
        if (handler == null)
            return MatchResult.fail("游戏类型错误");
        Player p1 = match.getP1();
        Player p2 = match.getP2();
        handler.end(match);
        String message = """
                ⚠️%s对局强制终止
                - 玩家1: %s
                - 玩家2: %s
                - MatchID: %s"""
                .formatted(match.getType(), p1.getId(), p2.getId(), matchId);
        return MatchResult.success(p1.getInProgressGroupId(), p2.getInProgressGroupId(), message);
    }

    @Scheduled(fixedDelay = 10_000)
    public void cleanup() {
        cleanWaitingPlayers();
        cleanTimeoutMatches();
    }

    private void cleanWaitingPlayers() {
        matchingPool.clean(WAITING_TIMEOUT_SECONDS, p -> {
            log.warn("▽ [GameEngine] 清理匹配超时玩家 - PlayerID: {}", p.getId());
            playerManager.reset(p.getId());
            String message = "%s(%s) 匹配超时".formatted(p.getName(), p.getId());
            botOperator.sendGroupMsg(p.getInProgressGroupId(), message);
        });
    }

    private void cleanTimeoutMatches() {
        matchManager.clean(PLAYING_TIMEOUT_SECONDS, m -> {
            log.warn("▽ [GameEngine] 清理响应超时对局 - MatchID: {}", m.getId());
            Player p1 = m.getP1();
            Player p2 = m.getP2();
            String info = """
                    ⚠️对局已超时
                    - P1: %s(%s)
                    - P2: %s(%s)
                    - MatchID: %s"""
                    .formatted(p1.getName(), p1.getId(), p2.getName(), p2.getId(), m.getId());
            if (!Objects.equals(p1.getInProgressGroupId(), p2.getInProgressGroupId()))
                botOperator.sendGroupMsg(p1.getInProgressGroupId(), info);
            botOperator.sendGroupMsg(p2.getInProgressGroupId(), info);
            GameHandler<?, ?, ?> handler = handlerRegistry.get(m.getType());
            if (handler != null) handler.end(m);
        });
    }
}
