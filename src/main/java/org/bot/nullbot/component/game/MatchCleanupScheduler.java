package org.bot.nullbot.component.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.MatchConfig;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchCleanupScheduler
{
    private final MatchPoolManager poolManager;
    private final MatchManager matchManager;
    private final PlayerManager playerManager;
    private final MatchConfig matchConfig;

    /**
     * 每 30 秒扫描一次
     */
    @Scheduled(fixedDelay = 30_000)
    public void cleanup() {
        cleanWaitingPlayers();
        cleanTimeoutMatches();
    }

    /**
     * 清理等待匹配超时的玩家
     */
    private void cleanWaitingPlayers() {
        LocalDateTime now = LocalDateTime.now();

        poolManager.getAllPools().forEach((gameType, queue) -> {

            Iterator<Player> iterator = queue.iterator();

            while (iterator.hasNext()) {
                Player p = iterator.next();

                if (p.getStatus() != Player.PlayerStatus.WAITING) {
                    iterator.remove();
                    continue;
                }

                if (p.getWaitingSince() == null) {
                    continue;
                }

                long seconds = Duration.between(p.getWaitingSince(), now).getSeconds();

                if (seconds >= matchConfig.getWaitingTimeoutSeconds()) {
                    iterator.remove();
                    p.setStatus(Player.PlayerStatus.IDLE);
                    p.setWaitingSince(null);

                    log.info("玩家 {} 等待匹配超时，已移除", p.getUserName());
                    // TODO：推送 QQ 消息提示
                }
            }
        });
    }

    /**
     * 清理对局超时（无操作）
     */
    private void cleanTimeoutMatches() {
        LocalDateTime now = LocalDateTime.now();

        matchManager.getAllMatches().forEach(match -> {

            if (match.getStatus() != Match.MatchStatus.PLAYING) {
                return;
            }

            LocalDateTime lastAction = match.getLastActionTime();
            if (lastAction == null) {
                lastAction = match.getStartTime();
            }

            long seconds = Duration.between(lastAction, now).getSeconds();

            if (seconds >= matchConfig.getPlayingTimeoutSeconds()) {
                log.warn("Match {} 超时，强制结束", match.getMatchId());

                forceFinishMatch(match);
            }
        });
    }

    private void forceFinishMatch(Match match) {
        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndTime(LocalDateTime.now());

        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();

        resetPlayer(p1);
        resetPlayer(p2);

        matchManager.remove(match.getMatchId());

        // TODO：通知群聊 & 玩家
    }

    private void resetPlayer(Player p) {
        p.setStatus(Player.PlayerStatus.IDLE);
        p.setInProgressMatchId(null);
        p.setWaitingSince(null);
    }
}
