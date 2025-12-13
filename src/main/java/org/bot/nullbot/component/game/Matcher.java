package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;


@Component
public class Matcher
{
    private final PlayerManager playerManager;
    private final MatchPoolManager poolManager;
    private final MatchManager matchManager;

    // gameType -> match handler
    private final Map<String, GameMatchHandler> handlerMap = new HashMap<>();

    public Matcher(
            PlayerManager playerManager,
            MatchPoolManager poolManager,
            MatchManager matchManager,
            List<GameMatchHandler> handlers
    ) {
        this.playerManager = playerManager;
        this.poolManager = poolManager;
        this.matchManager = matchManager;

        // 自动注册所有 Handler
        handlers.forEach(h -> handlerMap.put(h.getClass().getSimpleName().replace("MatchHandler", "").toLowerCase(), h));
    }

    /**
     * 玩家加入匹配
     */
    public String joinMatch(Long userId, Long groupId, String userName, String gameType) {

        Player player = playerManager.getOrCreate(userId, groupId, userName);

        if (player.getStatus() != Player.PlayerStatus.IDLE) {
            return "你已经在匹配或游戏中！";
        }

        GameMatchHandler handler = handlerMap.get(gameType);
        if (handler == null) {
            return "暂不支持该类型游戏：" + gameType;
        }

        Queue<Player> queue = poolManager.getPool(gameType);

        // 尝试匹配另一个玩家
        Player other = queue.poll();

        if (other == null) {
            // 加入等待队列
            poolManager.addPlayer(player, gameType);
            playerManager.updateStatus(player, Player.PlayerStatus.WAITING);
            return "已加入 " + gameType + " 匹配队列，正在等待对手…";
        }

        // 自定义匹配规则（例如某游戏必须分段匹配等）
        if (!handler.canMatch(player, other)) {
            // 不适配，other 继续入队
            poolManager.addPlayer(other, gameType);
            poolManager.addPlayer(player, gameType);
            return "暂时无法匹配到合适的玩家，已重新加入队列";
        }

        // 匹配成功
        Match match = matchManager.createMatch(gameType, player, other);

        player.setStatus(Player.PlayerStatus.PLAYING);
        other.setStatus(Player.PlayerStatus.PLAYING);

        player.setInProgressMatchId(match.getMatchId());
        other.setInProgressMatchId(match.getMatchId());

        handler.onMatchStart(match);

        matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        return String.format("匹配成功！游戏类型：%s\n玩家1：%s\n玩家2：%s\nMatchID=%s",
                gameType, player.getUserName(), other.getUserName(), match.getMatchId());
    }

    /**
     * 结束游戏
     */
    public String finishMatch(String matchId) {
        Match match = matchManager.getMatch(matchId);
        if (match == null) {
            return "Match 不存在";
        }

        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndTime(LocalDateTime.now());

        GameMatchHandler handler = handlerMap.get(match.getGameType());
        if (handler != null) {
            handler.onMatchEnd(match);
        }

        // 清理玩家状态
        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();

        p1.setStatus(Player.PlayerStatus.IDLE);
        p1.setInProgressMatchId(null);

        p2.setStatus(Player.PlayerStatus.IDLE);
        p2.setInProgressMatchId(null);

        matchManager.finishMatch(matchId);

        return "Match 已结束：" + matchId;
    }
}
