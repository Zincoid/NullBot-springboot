package org.bot.nullbot.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.game.manager.MatchManager;
import org.bot.nullbot.component.game.manager.MatchPoolManager;
import org.bot.nullbot.component.game.manager.PlayerManager;
import org.bot.nullbot.config.MatchProperties;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class MatchCleanupScheduler
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final MatchPoolManager poolManager;
    private final MatchManager matchManager;
    private final PlayerManager playerManager;
    private final MatchProperties matchProperties;

    // gameType -> match handler
    private final Map<String, GameMatchHandler> handlerMap = new HashMap<>();

    public MatchCleanupScheduler(
            BotContainer botContainer,
            MatchPoolManager poolManager,
            MatchManager matchManager,
            PlayerManager playerManager,
            MatchProperties matchProperties,
            List<GameMatchHandler> handlers
    ) {
        this.botContainer = botContainer;
        this.poolManager = poolManager;
        this.matchManager = matchManager;
        this.playerManager = playerManager;
        this.matchProperties = matchProperties;

        // 自动注册所有 Handler
        handlers.forEach(h -> handlerMap.put(h.gameType(), h));
    }

    /**
     * 每 10 秒清理一次超时
     */
    @Scheduled(fixedDelay = 10_000)
    public void cleanup() {
        // log.info("[MatchCleanupScheduler] 超时清理触发");
        cleanWaitingPlayers();
        cleanTimeoutMatches();
    }

    /**
     * 清理等待匹配超时的玩家
     */
    private void cleanWaitingPlayers() {
        Bot bot = botContainer.robots.get(botId);
        LocalDateTime now = LocalDateTime.now();
        poolManager.getAllPools().forEach((gameType, queue) -> queue.removeIf(p -> {
            long seconds = Duration.between(p.getLastActionTime(), now).getSeconds();
            if (seconds >= matchProperties.getWaitingTimeoutSeconds()) {
                log.info("◉ [Match Cleaner] 清理匹配超时玩家 {}", p.getUserId());
                bot.sendGroupMsg(p.getGroupId(), p.getUserName() + "(" + p.getUserId() + ") 匹配超时！", false);
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
        Bot bot = botContainer.robots.get(botId);
        LocalDateTime now = LocalDateTime.now();
        matchManager.getAllMatches().forEach(match -> {
            if (match.getStatus() != Match.MatchStatus.PLAYING) { return; }
            LocalDateTime lastActionTime = match.getLastActionTime();
            if (lastActionTime == null) lastActionTime = match.getStartTime();
            long seconds = Duration.between(lastActionTime, now).getSeconds();
            if (seconds >= matchProperties.getPlayingTimeoutSeconds()) {
                log.warn("◉ [Match Cleaner] Match {} 超时未响应自动结束", match.getMatchId());
                Player p1 = match.getPlayer1();
                Player p2 = match.getPlayer2();
                String info = "对局已超时！玩家:\n" + p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\nMatch ID: " + match.getMatchId();
                if(!Objects.equals(p1.getGroupId(), p2.getGroupId())){
                    bot.sendGroupMsg(p1.getGroupId(), info, false);
                }
                bot.sendGroupMsg(p2.getGroupId(), info, false);

                // 在对应游戏执行器中触发对局结束流程
                handlerMap.get(match.getGameType()).onMatchEnd(match);
            }
        });
    }
}
