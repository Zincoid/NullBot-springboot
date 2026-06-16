package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.GameRes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GameHandler<M extends Match, S extends GameState, L extends GameLogic<M, S>, R extends GameRenderer<S>> {

    protected static final ThreadLocal<Match> CURRENT_MATCH = new ThreadLocal<>();
    protected static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

    protected final Map<String, S> states = new ConcurrentHashMap<>();  // MatchId -> GameState
    protected final L logic;
    protected final R renderer;

    protected final BotOperator botOperator;
    protected final MatchManager matchManager;
    protected final PlayerManager playerManager;

    public abstract String getType();

    public abstract GameMode getMode();

    // ================== 对局控制方法 ==================

    @SuppressWarnings("unchecked")
    public final void start(Match match) {
        M m = (M) match;
        S state = logic.create(m);
        states.put(m.getId(), state);
        for (Player p : m.getPlayers())
            playerManager.update(p.getId(), Player.PlayerStatus.PLAYING);
        matchManager.update(m.getId(), Match.MatchStatus.PLAYING);
        onStart(m, state);
    }

    @SuppressWarnings("unchecked")
    public final void end(Match match) {
        M m = (M) match;
        S state = states.remove(m.getId());
        for (Player p : m.getPlayers())
            playerManager.reset(p.getId());
        matchManager.remove(m.getId());
        if (state == null || !state.isFinished()) return;
        onEnd(m, state);
    }

    @SuppressWarnings("unchecked")
    public final GameRes act(Long userId, CmdArgs args) {
        Match match = matchManager.get(userId);
        if (match == null) return fail("对局不存在");
        S state = states.get(match.getId());
        if (state == null) return fail("状态不存在");
        Player self = playerManager.get(userId);
        M m = (M) match;
        CURRENT_MATCH.set(m);
        CURRENT_PLAYER.set(self);
        try {
            return onAction(m, state, self, args);
        } finally {
            CURRENT_MATCH.remove();
            CURRENT_PLAYER.remove();
        }
    }

    // ================== 逻辑抽象方法 ==================

    protected abstract void onStart(M match, S state);

    protected abstract void onEnd(M match, S state);

    protected abstract GameRes onAction(M match, S state, Player self, CmdArgs args);

    // ================== 响应构建方法 ==================

    // ---------------- 通用错误响应方法 ----------------

    protected final GameRes fail(String message) {
        Player self = CURRENT_PLAYER.get();
        return GameRes.fail(self.getInProgressGroupId(), message);
    }

    // ---------------- 单人模式响应方法 ----------------

    protected abstract GameRes success(String msg);

    protected abstract GameRes finish(String msg);

    // ---------------- 双人模式响应方法 ----------------

    protected abstract GameRes success(boolean async, String self, String opp);

    protected abstract GameRes finish(boolean async, String self, String opp);


    // ================== 输入监听方法 ==================

    public final boolean isActive(String matchId) {
        return states.containsKey(matchId);
    }

    public abstract String getPattern();
}
