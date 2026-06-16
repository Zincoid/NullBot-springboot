package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.core.enums.GameMode;
import com.zincoid.nullbot.core.module.game.model.*;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.system.BotOperator;

public abstract class SoloHandler<S extends GameState, L extends GameLogic<SoloMatch, S>, R extends GameRenderer<S>>
        extends GameHandler<SoloMatch, S, L, R> {

    protected SoloHandler(L gameLogic, R renderer, BotOperator botOperator,
                          MatchManager matchManager, PlayerManager playerManager) {
        super(gameLogic, renderer, botOperator, matchManager, playerManager);
    }

    @Override
    public final GameMode getMode() {
        return GameMode.SOLO;
    }

    @Override
    protected final GameRes success(boolean async, String self, String opp) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    @Override
    protected final GameRes finish(boolean async, String self, String opp) {
        throw new UnsupportedOperationException("不支持的模式响应");
    }

    @Override
    protected final GameRes success(String msg) {
        Player self = CURRENT_PLAYER.get();
        return GameRes.success().add(self.getInProgressGroupId(), msg);
    }

    @Override
    protected final GameRes finish(String msg) {
        SoloMatch match = (SoloMatch) CURRENT_MATCH.get();
        Player self = CURRENT_PLAYER.get();
        GameRes result = GameRes.success()
                .add(self.getInProgressGroupId(), msg + "\n\n对局已结束: " + match.getId());
        end(match);
        return result;
    }
}
