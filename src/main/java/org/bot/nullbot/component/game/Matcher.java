package org.bot.nullbot.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import org.bot.nullbot.entity.game.basic.MatchResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class Matcher
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final PlayerManager playerManager;
    private final MatchManager matchManager;
    private final MatchPoolManager poolManager;

    // gameType -> match handler
    private final Map<String, GameMatchHandler> handlerMap = new HashMap<>();

    public Matcher(
            BotContainer botContainer,
            PlayerManager playerManager,
            MatchPoolManager poolManager,
            MatchManager matchManager,
            List<GameMatchHandler> handlers
    ) {
        this.botContainer = botContainer;
        this.playerManager = playerManager;
        this.poolManager = poolManager;
        this.matchManager = matchManager;

        // 自动注册所有 Handler
        handlers.forEach(h -> handlerMap.put(h.getClass().getSimpleName().replace("StateHandler", "").toLowerCase(), h));
    }

    /**
     * 加入匹配
     */
    public MatchResult joinMatch(Long userId, Long groupId, String userName, String gameType) {
        Player player = playerManager.refreshAndGetPlayer(userId, groupId, userName);

        if (player.getStatus() != Player.PlayerStatus.IDLE) { return MatchResult.notMatched("你已经在匹配或游戏中！"); }

        GameMatchHandler handler = handlerMap.get(gameType);
        if (handler == null) { return MatchResult.notMatched("暂不支持该类型游戏：" + gameType); }

        // 尝试匹配另一个玩家
        Player other = poolManager.pollPlayer(gameType);

        if (other == null) {
            // 加入等待队列
            poolManager.addPlayer(player, gameType);
            playerManager.updateStatus(player, Player.PlayerStatus.WAITING);
            return MatchResult.notMatched("已加入 " + gameType + " 匹配队列，正在等待对手…");
        }

        // 判断 handler 中自定义的匹配规则
        if (!handler.canMatch(player, other)) {
            // 不适配，other 继续入队
            poolManager.addPlayer(other, gameType);
            poolManager.addPlayer(player, gameType);
            return MatchResult.notMatched("暂时无法匹配到合适的玩家，已重新加入队列");
        }

        // 匹配成功 创建对局
        Match match = matchManager.createMatch(gameType, player, other);

        // 为双方设置对局ID
        player.setInProgressMatchId(match.getMatchId());
        other.setInProgressMatchId(match.getMatchId());

        // 初始化对应游戏模式的数据
        handler.onMatchStart(match);

        // 更新双方玩家状态
        playerManager.updateStatus(player, Player.PlayerStatus.PLAYING);
        playerManager.updateStatus(other, Player.PlayerStatus.PLAYING);

        // 开始游戏 更新对局状态
        matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        String info = String.format("匹配成功！游戏类型：%s\n玩家1：%s\n玩家2：%s\nMatch ID: %s",
                gameType, player.getUserName(), other.getUserName(), match.getMatchId());
        return MatchResult.matched(player.getGroupId(), other.getGroupId(), info);
    }

    /**
     * 取消匹配
     */
    public MatchResult cancelMatch(Long userId) {
        Player player = playerManager.getPlayer(userId);
        if (player == null) { return MatchResult.notMatched("暂无玩家记录"); }
        if (player.getStatus() != Player.PlayerStatus.WAITING) { return MatchResult.notMatched("无法取消，当前不在匹配队列中"); }

        // 从匹配池中移除
        if (!poolManager.removePlayer(player)) { return MatchResult.notMatched("取消失败，未在匹配队列中找到你"); }
        // 重置玩家状态
        playerManager.updateStatus(player, Player.PlayerStatus.IDLE);

        return MatchResult.notMatched("已成功取消匹配");
    }

    /**
     * 结束游戏 通过用户ID
     */
    public MatchResult finishMatchByPlayerId(Long userId) {
        String matchId = playerManager.getPlayer(userId).getInProgressMatchId();
        Match match = matchManager.getMatch(matchId);
        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();
        Bot bot = botContainer.robots.get(botId);
        if(!Objects.equals(p1.getGroupId(), p2.getGroupId())){
            bot.sendGroupMsg(p1.getGroupId(), p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\n对局已被终止", false);
        }
        bot.sendGroupMsg(p2.getGroupId(), p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\n对局已被终止", false);
        return finishMatch(matchId);
    }

    /**
     * 结束游戏 通过对局ID
     */
    public MatchResult finishMatch(String matchId) {
        Match match = matchManager.getMatch(matchId);
        if (match == null) { return MatchResult.notMatched("Match 不存在"); }

        // 清理游戏数据
        GameMatchHandler handler = handlerMap.get(match.getGameType());
        if (handler != null) { handler.onMatchEnd(match); }

        // 重置玩家状态
        playerManager.resetPlayer(match.getPlayer1());
        playerManager.resetPlayer(match.getPlayer2());

        // 移除游戏会话
        matchManager.removeMatch(matchId);

        return MatchResult.notMatched("Match 已结束：" + matchId);
    }
}
