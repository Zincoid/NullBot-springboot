package com.zincoid.nullbot.core.module.game.framework.handler;

import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.framework.Handler;
import com.zincoid.nullbot.core.module.game.framework.Logic;
import com.zincoid.nullbot.core.module.game.framework.Renderer;
import com.zincoid.nullbot.core.module.game.framework.State;
import com.zincoid.nullbot.core.module.game.model.*;
import com.zincoid.nullbot.core.module.game.model.match.DualMatch;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.system.BotOperator;

public abstract class DualHandler<S extends State, L extends Logic<DualMatch, S>, R extends Renderer<S>>
        extends Handler<DualMatch, S, L, R> {

    protected DualHandler(L logic, R renderer, BotOperator botOperator,
                          MatchManager matchManager, PlayerManager playerManager) {
        super(logic, renderer, botOperator, matchManager, playerManager);
    }

    @Override
    public final GameMode getMode() {
        return GameMode.DUAL;
    }

    @Override
    protected final Result success(boolean async, String self, String opp) {
        Match match = CURRENT_MATCH.get();
        Player _self = CURRENT_PLAYER.get();
        DualMatch dm = (DualMatch) match;
        Player _opp = playerManager.get(dm.getP1().getId().equals(_self.getId())
                ? dm.getP2().getId()
                : dm.getP1().getId());
        Result res = Result.success().add(_self.getInProgressGroupId(), self);
        if (async || !_self.getInProgressGroupId().equals(_opp.getInProgressGroupId()))
            res.add(_opp.getInProgressGroupId(), async ? opp : self);
        return res;
    }

    @Override
    protected final Result finish(boolean async, String self, String opp) {
        DualMatch match = (DualMatch) CURRENT_MATCH.get();
        Result result = success(
                async,
                self + "\n\n对局已结束: " + match.getId(),
                opp + "\n\n对局已结束: " + match.getId()
        );
        end(match);
        return result;
    }
}
