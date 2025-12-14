package org.bot.nullbot.component.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public abstract class GameMatchHandler<T>
{
    protected Long botId;
    protected BotContainer botContainer;

    protected final MatchManager matchManager;

    // matchId -> game state
    protected final Map<String, T> games;

    protected GameMatchHandler(Long botId, BotContainer botContainer, MatchManager matchManager) {
        this.botId = botId;
        this.botContainer = botContainer;
        this.matchManager = matchManager;
        games = new ConcurrentHashMap<>();
    }

    // 发送初始信息方法
    protected void sendInitMessage(Match match, T state){
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

    public abstract String gameType();

    // 判断是否能够匹配
    public abstract boolean canMatch(Player p1, Player p2);

    // 游戏开始前初始化
    public abstract void onMatchStart(Match match);

    // 游戏结束后的清理
    public abstract void onMatchEnd(Match match);

    // 游戏输出渲染方法
    protected abstract String render(T state);
}
