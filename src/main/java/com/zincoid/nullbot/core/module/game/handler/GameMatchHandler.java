package com.zincoid.nullbot.core.module.game.handler;

import com.zincoid.nullbot.core.module.game.logic.GameLogic;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.AllArgsConstructor;
import com.zincoid.nullbot.core.module.game.manager.MatchManager;
import com.zincoid.nullbot.core.module.game.manager.PlayerManager;
import com.zincoid.nullbot.core.module.game.state.GameState;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public abstract class GameMatchHandler<S extends GameState, L extends GameLogic<S>> {

    protected final BotOperator botOperator;
    protected final MatchManager matchManager;
    protected final PlayerManager playerManager;
    protected final L gameLogic;

    protected final Map<String, S> games = new ConcurrentHashMap<>();  // matchId -> game state

    // 定义游戏类型名称
    public abstract String gameType();

    // 游戏开始前初始化
    public void onMatchStart(Match match) {
        S state = gameLogic.create(match);
        games.put(match.getMatchId(), state);
        // 更新玩家状态
        playerManager.update(match.getPlayer1().getId(), Player.PlayerStatus.PLAYING);
        playerManager.update(match.getPlayer2().getId(), Player.PlayerStatus.PLAYING);
        // 更新对局状态
        matchManager.update(match.getMatchId(), Match.MatchStatus.PLAYING);
        sendInitMessage(match, state);
    }

    // 游戏结束后的清理
    public void onMatchEnd(Match match) {
        // 移除游戏数据
        games.remove(match.getMatchId());
        // 重置玩家状态
        playerManager.reset(match.getPlayer1().getId());
        playerManager.reset(match.getPlayer2().getId());
        // 移除游戏会话
        matchManager.remove(match.getMatchId());
    }

    // 游戏输出渲染方法
    protected abstract String render(S state);

    // ======== 接入 Logic 的方法 在子类中添加 ========

    // ================== 通用方法 ==================

    // 发送初始同步信息方法
    protected void sendInitMessage(Match match, S state){
        Player p1 = match.getPlayer1();
        Player p2 = match.getPlayer2();
        String info = render(state);
        if (!Objects.equals(p1.getInProgressGroupId(), p2.getInProgressGroupId()))
            botOperator.sendGroupMsg(p1.getInProgressGroupId(), info);
        botOperator.sendGroupMsg(p2.getInProgressGroupId(), info);
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
                selfInfo, opponentInfo
        );
    }

    // 成功消息结果 (自动结束游戏)
    protected GameResult getFinishResult(Long userId, Match match, Boolean isAsync,
                                         String selfInfo, String opponentInfo) {
        GameResult result = getSuccessResult(
                userId, match, isAsync,
                selfInfo + "\n\nMatch 已结束：" + match.getMatchId(),
                opponentInfo + "\n\nMatch 已结束：" + match.getMatchId()
        );
        onMatchEnd(match);
        return result;
    }
}
