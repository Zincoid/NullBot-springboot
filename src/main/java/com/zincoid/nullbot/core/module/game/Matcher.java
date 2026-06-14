package com.zincoid.nullbot.core.module.game;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.module.game.handler.GameMatchHandler;
import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.MatchPoolManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.model.result.MatchResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Matcher {

    private final BotOperator botOperator;
    private final PlayerManager playerManager;
    private final MatchManager matchManager;
    private final MatchPoolManager poolManager;
    private final HandlerRegistry handlerRegistry;

    /**
     * 加入匹配
     */
    public MatchResult joinMatch(Long userId, Long groupId, String userName, String gameType) {
        Player player = playerManager.refreshAndGetPlayer(userId, groupId, userName);
        if (player.getStatus() != Player.PlayerStatus.IDLE)
            return MatchResult.notMatched("你已经在匹配或游戏中！");

        GameMatchHandler<?, ?> handler = handlerRegistry.get(gameType);
        if (handler == null)
            return MatchResult.notMatched("暂不支持该类型游戏");

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

        // 初始化游戏对局和玩家状态
        handler.onMatchStart(match);

        String info = String.format("匹配成功！游戏类型：%s\n玩家1：%s\n玩家2：%s\nMatch ID: %s",
                gameType, player.getUserName(), other.getUserName(), match.getMatchId());
        return MatchResult.matched(player.getGroupId(), other.getGroupId(), info);
    }

    /**
     * 取消匹配
     */
    public MatchResult cancelMatch(Long userId) {
        Player player = playerManager.getPlayer(userId);
        if (player == null)
            return MatchResult.notMatched("暂无玩家记录");
        if (player.getStatus() != Player.PlayerStatus.WAITING)
            return MatchResult.notMatched("无法取消，非等待匹配状态");

        // 从匹配池中移除
        if (!poolManager.removePlayer(player))
            return MatchResult.notMatched("取消失败，不在匹配队列中");
        // 重置玩家状态
        playerManager.updateStatus(player, Player.PlayerStatus.IDLE);

        return MatchResult.notMatched("已成功取消匹配");
    }

    /**
     * 结束对局
     */
    public boolean finishMatch(Long userId) {
        Player player = playerManager.getPlayer(userId);
        if (player == null) return false;
        String matchId = player.getInProgressMatchId();
        if (matchId == null) return false;
        Match match = matchManager.getMatch(matchId);
        if (match == null) return false;
        GameMatchHandler<?, ?> handler = handlerRegistry.get(match.getGameType());
        if (handler == null) return false;

        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();

        // 在对应游戏执行器中触发对局结束流程
        handler.onMatchEnd(match);
        // 发送终止提醒
        String info = p1.getUserName() + "(" + p1.getUserId() + ")\n" + p2.getUserName() + "(" + p2.getUserId() + ")\n对局已被终止";
        if (!Objects.equals(p1.getGroupId(), p2.getGroupId()))
            botOperator.sendGroupMsg(p1.getGroupId(), info);
        botOperator.sendGroupMsg(p2.getGroupId(), info);

        return true;
    }
}
