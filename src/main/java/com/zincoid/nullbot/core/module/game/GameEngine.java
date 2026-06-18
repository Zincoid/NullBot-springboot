package com.zincoid.nullbot.core.module.game;

import com.zincoid.nullbot.core.module.game.framework.Handler;
import com.zincoid.nullbot.core.module.game.runtime.InputListener;
import com.zincoid.nullbot.core.module.game.runtime.*;
import com.zincoid.nullbot.core.module.game.model.match.DualMatch;
import com.zincoid.nullbot.core.module.game.model.Result;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.game.model.match.SoloMatch;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

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
    private final InputListener inputListener;

    public Result join(Long groupId, Long userId, String userName, String type) {
        Player self = playerManager.set(userId, groupId, userName);
        if (self.getStatus() != Player.PlayerStatus.IDLE)
            return Result.fail(groupId, "已在匹配或游戏中");
        Handler<?, ?, ?, ?> handler = handlerRegistry.get(type);
        if (handler == null)
            return Result.fail(groupId, "不支持该游戏类型");
        return switch (handler.getMode()) {
            case DUAL -> joinDual(self, type, handler);
            case SOLO -> joinSolo(self, type, handler);
        };
    }

    private Result joinDual(Player self, String type, Handler<?, ?, ?, ?> handler) {
        Player opp = matchingPool.poll(type);
        if (opp == null) {
            matchingPool.add(self.getId(), type);
            playerManager.update(self.getId(), Player.PlayerStatus.WAITING);
            return Result.success().add(self.getInProgressGroupId(), "已加入匹配队列...");
        }
        DualMatch match = matchManager.createDual(self.getId(), opp.getId(), type);
        self.setInProgressMatchId(match.getId());
        opp.setInProgressMatchId(match.getId());
        handler.start(match);
        inputListener.listen(match, handler);
        String message = """
                ✅%s对局匹配成功
                - P1: %s(%s)
                - P2: %s(%s)
                - MatchID: %s"""
                .formatted(type, self.getName(), self.getId(), opp.getName(), opp.getId(), match.getId());
        return Result.success(Set.of(self.getInProgressGroupId(), opp.getInProgressGroupId()), message);
    }

    private Result joinSolo(Player self, String type, Handler<?, ?, ?, ?> handler) {
        SoloMatch match = matchManager.createSolo(self.getId(), type);
        self.setInProgressMatchId(match.getId());
        handler.start(match);
        inputListener.listen(match, handler);
        return Result.success().add(self.getInProgressGroupId(), "单人%s游戏已开始".formatted(type));
    }

    public Result cancel(Long groupId, Long userId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return Result.fail(groupId, "玩家暂未注册");
        if (player.getStatus() != Player.PlayerStatus.WAITING)
            return Result.fail(groupId, "非匹配中状态");
        if (!matchingPool.remove(userId))
            return Result.fail(groupId, "不在匹配队列");
        playerManager.update(userId, Player.PlayerStatus.IDLE);
        return Result.success().add(groupId, "取消匹配成功");
    }

    public Result finish(Long groupId, Long userId) {
        Player player = playerManager.get(userId);
        if (player == null)
            return Result.fail(groupId, "玩家暂未注册");
        String matchId = player.getInProgressMatchId();
        if (matchId == null)
            return Result.fail(groupId, "玩家未在游戏");
        Match match = matchManager.get(matchId);
        if (match == null)
            return Result.fail(groupId, "对局信息错误");
        Handler<?, ?, ?, ?> handler = handlerRegistry.get(match.getType());
        if (handler == null)
            return Result.fail(groupId, "游戏类型错误");
        handler.end(match);
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️%s对局强制终止\n".formatted(match.getType()));
        for (Player p : match.getPlayers())
            sb.append("- %s(%s)\n".formatted(p.getName(), p.getId()));
        sb.append("- MatchID: %s".formatted(matchId));
        String message = sb.toString();
        Set<Long> groups = new HashSet<>();
        for (Player p : match.getPlayers())
            if (p.getInProgressGroupId() != null)
                groups.add(p.getInProgressGroupId());
        return Result.success(groups, message);
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
            StringBuilder sb = new StringBuilder("⚠️对局已超时\n");
            for (Player p : m.getPlayers())
                sb.append("- %s(%s)\n".formatted(p.getName(), p.getId()));
            sb.append("- MatchID: %s".formatted(m.getId()));
            String info = sb.toString();
            Set<Long> groups = new HashSet<>();
            for (Player p : m.getPlayers())
                if (groups.add(p.getInProgressGroupId()))
                    botOperator.sendGroupMsg(p.getInProgressGroupId(), info);
            Handler<?, ?, ?, ?> handler = handlerRegistry.get(m.getType());
            if (handler != null) handler.end(m);
        });
    }
}
