package org.bot.nullbot.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.AllArgsConstructor;
import org.bot.nullbot.entity.game.GameState;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.service.UserService;

import java.util.Map;
import java.util.Objects;


@AllArgsConstructor
public abstract class GameMatchHandler<S extends GameState, L extends GameLogic>
{
    protected Long botId;

    protected BotContainer botContainer;
    protected final MatchManager matchManager;
    protected final PlayerManager playerManager;
    protected final UserService userService;

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

    // ================== 通用方法 ==================

    // 发送初始信息方法
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

    // 返回跨群判断结果
    protected GameResult getGameResult(Long userId, Match match, String info){
        boolean sameGroup =
                match.getPlayer1().getGroupId()
                        .equals(match.getPlayer2().getGroupId());
        if (sameGroup) {
            return GameResult.success(null, info);
        }
        Long opponentGroupId =
                userId.equals(match.getPlayer1().getUserId())
                        ? match.getPlayer2().getGroupId()
                        : match.getPlayer1().getGroupId();

        return GameResult.success(opponentGroupId, info);
    }

    // 返回游戏结束结果 (自动结束游戏)
    protected GameResult getFinishResult(Long userId, Match match, String info) {
        GameResult result = getGameResult(userId, match, info + "\n\nMatch 已结束：" + match.getMatchId());
        onMatchEnd(match);
        return result;
    }
}
