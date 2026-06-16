package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.AllArgsConstructor;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.GameRes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public abstract class GameHandler<S extends GameState, L extends GameLogic<S>, R extends GameRenderer<S>> {

    protected static final ThreadLocal<Match> CURRENT_MATCH = new ThreadLocal<>();
    protected static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

    protected final Map<String, S> games = new ConcurrentHashMap<>();  // MatchId -> GameState

    protected final BotOperator botOperator;
    protected final MatchManager matchManager;
    protected final PlayerManager playerManager;
    protected final L gameLogic;
    protected final R renderer;

    public abstract String getType();

    // ================ 对局控制方法 ================

    public final void start(Match match) {
        S state = gameLogic.create(match);
        games.put(match.getId(), state);
        playerManager.update(match.getP1().getId(), Player.PlayerStatus.PLAYING);
        playerManager.update(match.getP2().getId(), Player.PlayerStatus.PLAYING);
        matchManager.update(match.getId(), Match.MatchStatus.PLAYING);
        onStart(match, state);
    }

    public final void end(Match match) {
        S state = games.remove(match.getId());
        playerManager.reset(match.getP1().getId());
        playerManager.reset(match.getP2().getId());
        matchManager.remove(match.getId());
        if (state == null || !state.isFinished()) return;
        onEnd(match, state);
    }

    public final GameRes act(Long userId, CmdArgs args) {
        Match match = matchManager.get(userId);
        if (match == null) return fail("对局不存在");
        S state = games.get(match.getId());
        if (state == null) return fail("状态不存在");
        Player self = playerManager.get(userId);
        Long oppId = match.getP1().getId().equals(userId)
                ? match.getP2().getId()
                : match.getP1().getId();
        Player opp = playerManager.get(oppId);
        CURRENT_MATCH.set(match);
        CURRENT_PLAYER.set(self);
        try {
            return onAction(state, self, opp, args);
        } finally {
            CURRENT_MATCH.remove();
            CURRENT_PLAYER.remove();
        }
    }

    // ================ 逻辑抽象方法 ================

    protected abstract void onStart(Match match, S state);

    protected abstract void onEnd(Match match, S state);

    protected abstract GameRes onAction(S state, Player self, Player opp, CmdArgs args);

    // ================ 响应构建方法 ================

    protected final GameRes fail(String message) {
        Player self = CURRENT_PLAYER.get();
        return GameRes.fail(self.getInProgressGroupId(), message);
    }

    protected final GameRes success(boolean async, String selfMessage, String oppMessage) {
        Match match = CURRENT_MATCH.get();
        Player self = CURRENT_PLAYER.get();
        Player opp = playerManager.get(match.getP1().getId().equals(self.getId())
                ? match.getP2().getId()
                : match.getP1().getId());
        return GameRes.success(
                async,
                self.getInProgressGroupId(),
                opp.getInProgressGroupId(),
                selfMessage, oppMessage
        );
    }

    protected final GameRes finish(boolean async, String selfMessage, String oppMessage) {
        Match match = CURRENT_MATCH.get();
        GameRes result = success(
                async,
                selfMessage + "\n\n对局已结束: " + match.getId(),
                oppMessage + "\n\n对局已结束: " + match.getId()
        );
        end(match);
        return result;
    }

    // ================ 输入监听方法 ================

    public final boolean isActive(String matchId) {
        return games.containsKey(matchId);
    }

    public abstract String getPattern();
}
