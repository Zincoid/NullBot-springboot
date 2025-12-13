package org.bot.nullbot.component.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.MatchConfig;
import org.bot.nullbot.entity.game.basic.Match;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchCleanupScheduler
{
    private final MatchPoolManager poolManager;
    private final MatchManager matchManager;
    private final PlayerManager playerManager;
    private final MatchConfig matchConfig;
    private final Matcher matcher;

    /**
     * 每 10 秒清理一次超时
     */
    @Scheduled(fixedDelay = 10_000)
    public void cleanup() {
        log.info("[MatchCleanupScheduler] 定时超时清理触发");
        cleanWaitingPlayers();
        cleanTimeoutMatches();
    }

    /**
     * 清理等待匹配超时的玩家
     */
    private void cleanWaitingPlayers() {
        LocalDateTime now = LocalDateTime.now();
        poolManager.getAllPools().forEach((gameType, queue) -> queue.removeIf(p -> {
            // if (p.getStatus() != Player.PlayerStatus.WAITING) { return true; }
            // if (p.getLastActionTime() == null) { return false; }  // 冗余
            long seconds = Duration.between(p.getLastActionTime(), now).getSeconds();
            if (seconds >= matchConfig.getWaitingTimeoutSeconds()) {
                log.info("清理匹配超时玩家: {}", p.getUserId());
                playerManager.resetPlayer(p);

                // TODO：通知群聊 & 玩家

                return true;
            }
            return false;
        }));
    }

    /**
     * 清理对局超时（无操作）
     */
    private void cleanTimeoutMatches() {
        LocalDateTime now = LocalDateTime.now();
        matchManager.getAllMatches().forEach(match -> {
            if (match.getStatus() != Match.MatchStatus.PLAYING) { return; }
            LocalDateTime lastActionTime = match.getLastActionTime();
            if (lastActionTime == null) lastActionTime = match.getStartTime();
            long seconds = Duration.between(lastActionTime, now).getSeconds();
            if (seconds >= matchConfig.getPlayingTimeoutSeconds()) {
                log.warn("Match {} 超时未响应自动结束", match.getMatchId());
                matcher.finishMatch(match.getMatchId());

                // TODO：通知群聊 & 玩家

            }
        });
    }
}
