package com.zincoid.nullbot.core.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.AllArgsConstructor;
import com.zincoid.nullbot.core.component.game.manager.MatchManager;
import com.zincoid.nullbot.core.component.game.manager.PlayerManager;
import com.zincoid.nullbot.core.entity.game.GameState;
import com.zincoid.nullbot.core.entity.result.GameResult;
import com.zincoid.nullbot.core.entity.game.basic.Match;
import com.zincoid.nullbot.core.entity.game.basic.Player;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public abstract class GameMatchHandler<S extends GameState, L extends GameLogic> {

    protected Long botId;

    protected BotContainer botContainer;
    protected final MatchManager matchManager;
    protected final PlayerManager playerManager;

    protected final L gameLogic;
    protected final Map<String, S> games;  // matchId -> game state

    // 定义游戏类型名称
    public abstract String gameType();

    // 判断是否能够匹配
    public boolean canMatch(Player p1, Player p2) { return true; }

    // 游戏开始前初始化
    public void onMatchStart(Match match) {
        // 更新玩家状态
        playerManager.updateStatus(match.getPlayer1(), Player.PlayerStatus.PLAYING);
        playerManager.updateStatus(match.getPlayer2(), Player.PlayerStatus.PLAYING);
        // 更新对局状态
        matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);
    }

    // 游戏结束后的清理
    public void onMatchEnd(Match match) {
        // 移除游戏数据
        games.remove(match.getMatchId());
        // 重置玩家状态
        playerManager.resetPlayer(match.getPlayer1());
        playerManager.resetPlayer(match.getPlayer2());
        // 移除游戏会话
        matchManager.removeMatch(match.getMatchId());
    }

    // 游戏输出渲染方法
    protected abstract String render(S state);

    // ========= 接入Logic的方法 在子类中添加 =========

    // ================== 通用方法 ==================

    // 发送初始同步信息方法
    protected void sendInitMessage(Match match, S state){
        Bot bot = botContainer.robots.get(botId);

        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();
        String info = render(state);

        if (!Objects.equals(p1.getGroupId(), p2.getGroupId())) {
            bot.sendGroupMsg(p1.getGroupId(), info, false);
        }
        bot.sendGroupMsg(p2.getGroupId(), info, false);
    }

    // 失败消息结果
    protected GameResult getErrorResult(String selfInfo) {
        return GameResult.error(selfInfo);
    }

    // 成功消息结果
    protected GameResult getSuccessResult(Long userId, Match match, Boolean isAsync,
                                          String selfInfo, String opponentInfo) {
        return GameResult.success(
                isAsync,
                match.getSelfGroupIdBySelfId(userId),
                match.getOpponentGroupIdBySelfId(userId),
                selfInfo,
                opponentInfo
        );
    }

    // 成功消息结果 (自动结束游戏)
    protected GameResult getFinishResult(Long userId, Match match, Boolean isAsync,
                                         String selfInfo, String opponentInfo) {
        GameResult result = getSuccessResult(
                userId,
                match,
                isAsync,
                selfInfo + "\n\nMatch 已结束：" + match.getMatchId(),
                opponentInfo + "\n\nMatch 已结束：" + match.getMatchId()
        );
        onMatchEnd(match);
        return result;
    }
}
