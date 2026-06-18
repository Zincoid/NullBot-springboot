package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.Result;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Handler<M extends Match, S extends State, L extends Logic<M, S>, R extends Renderer<S>> {

    protected static final ThreadLocal<Match> CURRENT_MATCH = new ThreadLocal<>();
    protected static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

    protected final Map<String, S> states = new ConcurrentHashMap<>();  // MatchId -> State
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
    public final Result act(Long userId, CmdArgs args) {
        Match match = matchManager.get(userId);
        if (match == null) return fail("对局不存在");
        S state = states.get(match.getId());
        if (state == null) return fail("状态不存在");
        Player self = playerManager.get(userId);
        CURRENT_MATCH.set(match);
        CURRENT_PLAYER.set(self);
        try {
            synchronized (state) {
                return onAction((M) match, state, self, args);
            }
        } finally {
            CURRENT_MATCH.remove();
            CURRENT_PLAYER.remove();
        }
    }

    // ================== 逻辑抽象方法 ==================

    protected abstract void onStart(M match, S state);

    protected abstract void onEnd(M match, S state);

    protected abstract Result onAction(M match, S state, Player self, CmdArgs args);

    // ================== 响应构建方法 ==================

    // ---------------- 通用错误响应方法 ----------------

    protected final Result fail(String message) {
        Player self = CURRENT_PLAYER.get();
        return Result.fail().add(self.getInProgressGroupId(), "❌" + message);
    }

    // ---------------- 单人模式响应方法 ----------------

    protected Result success(String msg) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    protected Result finish(String msg) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    // ---------------- 双人模式响应方法 ----------------

    protected Result success(boolean async, String self, String opp) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    protected Result finish(boolean async, String self, String opp) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }


    // ================== 输入监听方法 ==================

    public final boolean isActive(String matchId) {
        return states.containsKey(matchId);
    }

    public abstract String getPattern();
}
