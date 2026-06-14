package com.zincoid.nullbot.core.module.game;

import com.zincoid.nullbot.core.module.game.handler.GameMatchHandler;
import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.MatchPoolManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchCleaner {

    private static final long WAITING_TIMEOUT = 120;  // 匹配等待超时 (单位: Sec)
    private static final long PLAYING_TIMEOUT = 240;  // 游戏等待超时 (单位: Sec)

    private final BotOperator botOperator;
    private final MatchPoolManager poolManager;
    private final MatchManager matchManager;
    private final PlayerManager playerManager;
    private final HandlerRegistry handlerRegistry;

    /**
     * 定时清理玩家和对局 (每 10 Sec 清理一次)
     */
    @Scheduled(fixedDelay = 10_000)
    public void cleanup() {
        cleanWaitingPlayers();
        cleanTimeoutMatches();
    }

    /**
     * 等待匹配超时清理
     */
    private void cleanWaitingPlayers() {
        poolManager.removeTimeoutPlayers(WAITING_TIMEOUT, p -> {
            log.info("▽ [MatchCleaner] 清理匹配超时玩家 {}", p.getUserId());
            botOperator.sendGroupMsg(p.getGroupId(), p.getUserName() + "(" + p.getUserId() + ") 匹配超时");
            playerManager.resetPlayer(p);
        });
    }

    /**
     * 对局无操作超时清理
     */
    private void cleanTimeoutMatches() {
        LocalDateTime now = LocalDateTime.now();
        matchManager.getAllMatches().forEach(match -> {
            if (match.getStatus() != Match.MatchStatus.PLAYING) return;
            LocalDateTime lastActionTime = match.getLastActionTime();
            if (lastActionTime == null) lastActionTime = match.getCreateTime();
            long seconds = Duration.between(lastActionTime, now).getSeconds();
            if (seconds >= PLAYING_TIMEOUT) {
                log.warn("▽ [MatchCleaner] Match {} 超时未响应自动结束", match.getMatchId());
                Player p1 = match.getPlayer1();
                Player p2 = match.getPlayer2();
                String info = "对局已超时 玩家:\n" + p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\nMatch ID: " + match.getMatchId();
                if (!Objects.equals(p1.getGroupId(), p2.getGroupId()))
                    botOperator.sendGroupMsg(p1.getGroupId(), info);
                botOperator.sendGroupMsg(p2.getGroupId(), info);
                // 在对应游戏执行器中触发对局结束流程
                GameMatchHandler<?, ?> handler = handlerRegistry.get(match.getGameType());
                if (handler != null) handler.onMatchEnd(match);
            }
        });
    }
}
