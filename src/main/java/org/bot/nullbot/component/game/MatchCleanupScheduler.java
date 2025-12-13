package org.bot.nullbot.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.MatchConfig;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchCleanupScheduler
{
    @Value("${nullbot.self-id}")
    private Long selfId;
    private final BotContainer botContainer;

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
        Bot bot = botContainer.robots.get(selfId);
        LocalDateTime now = LocalDateTime.now();
        poolManager.getAllPools().forEach((gameType, queue) -> queue.removeIf(p -> {
            long seconds = Duration.between(p.getLastActionTime(), now).getSeconds();
            if (seconds >= matchConfig.getWaitingTimeoutSeconds()) {
                log.info("清理匹配超时玩家: {}", p.getUserId());
                bot.sendGroupMsg(p.getGroupId(), p.getUserName() + "(" + p.getUserId() + ") 匹配超时", false);
                playerManager.resetPlayer(p);
                return true;
            }
            return false;
        }));
    }

    /**
     * 清理对局超时（无操作）
     */
    private void cleanTimeoutMatches() {
        Bot bot = botContainer.robots.get(selfId);
        LocalDateTime now = LocalDateTime.now();
        matchManager.getAllMatches().forEach(match -> {
            if (match.getStatus() != Match.MatchStatus.PLAYING) { return; }
            LocalDateTime lastActionTime = match.getLastActionTime();
            if (lastActionTime == null) lastActionTime = match.getStartTime();
            long seconds = Duration.between(lastActionTime, now).getSeconds();
            if (seconds >= matchConfig.getPlayingTimeoutSeconds()) {
                log.warn("Match {} 超时未响应自动结束", match.getMatchId());
                Player p1 = match.getPlayer1();
                Player p2 = match.getPlayer2();
                if(!Objects.equals(p1.getGroupId(), p2.getGroupId())){
                    bot.sendGroupMsg(p1.getGroupId(), p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\n对局已超时", false);
                }
                bot.sendGroupMsg(p2.getGroupId(), p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\n对局已超时", false);
                matcher.finishMatch(match.getMatchId());
            }
        });
    }
}
