package com.zincoid.nullbot.core.module.game.framework.handler;

import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.framework.Handler;
import com.zincoid.nullbot.core.module.game.framework.Logic;
import com.zincoid.nullbot.core.module.game.framework.Renderer;
import com.zincoid.nullbot.core.module.game.framework.State;
import com.zincoid.nullbot.core.module.game.model.*;
import com.zincoid.nullbot.core.module.game.model.match.SoloMatch;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.system.BotOperator;

public abstract class SoloHandler<S extends State, L extends Logic<SoloMatch, S>, R extends Renderer<S>>
        extends Handler<SoloMatch, S, L, R> {

    protected SoloHandler(L logic, R renderer, BotOperator botOperator,
                          MatchManager matchManager, PlayerManager playerManager) {
        super(logic, renderer, botOperator, matchManager, playerManager);
    }

    @Override
    public final GameMode getMode() {
        return GameMode.SOLO;
    }

    @Override
    protected final Result success(String msg) {
        Player self = CURRENT_PLAYER.get();
        return Result.success().add(self.getInProgressGroupId(), msg);
    }

    @Override
    protected final Result finish(String msg) {
        SoloMatch match = (SoloMatch) CURRENT_MATCH.get();
        Player self = CURRENT_PLAYER.get();
        Result result = Result.success()
                .add(self.getInProgressGroupId(), msg + "\n\n对局已结束: " + match.getId());
        end(match);
        return result;
    }
}
