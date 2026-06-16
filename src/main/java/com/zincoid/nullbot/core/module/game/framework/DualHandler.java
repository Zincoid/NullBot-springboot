package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.model.*;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.system.BotOperator;

public abstract class DualHandler<S extends GameState, L extends GameLogic<DualMatch, S>, R extends GameRenderer<S>>
        extends GameHandler<DualMatch, S, L, R> {

    protected DualHandler(L gameLogic, R renderer, BotOperator botOperator,
                          MatchManager matchManager, PlayerManager playerManager) {
        super(gameLogic, renderer, botOperator, matchManager, playerManager);
    }

    @Override
    public final GameMode getMode() {
        return GameMode.DUAL;
    }

    @Override
    protected final GameRes success(boolean async, String self, String opp) {
        Match match = CURRENT_MATCH.get();
        Player _self = CURRENT_PLAYER.get();
        DualMatch dm = (DualMatch) match;
        Player _opp = playerManager.get(dm.getP1().getId().equals(_self.getId())
                ? dm.getP2().getId()
                : dm.getP1().getId());
        GameRes res = GameRes.success().add(_self.getInProgressGroupId(), self);
        if (async || !_self.getInProgressGroupId().equals(_opp.getInProgressGroupId()))
            res.add(_opp.getInProgressGroupId(), async ? opp : self);
        return res;
    }

    @Override
    protected final GameRes finish(boolean async, String self, String opp) {
        DualMatch match = (DualMatch) CURRENT_MATCH.get();
        GameRes result = success(
                async,
                self + "\n\n对局已结束: " + match.getId(),
                opp + "\n\n对局已结束: " + match.getId()
        );
        end(match);
        return result;
    }

    @Override
    protected final GameRes success(String msg) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    @Override
    protected final GameRes finish(String msg) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }
}
