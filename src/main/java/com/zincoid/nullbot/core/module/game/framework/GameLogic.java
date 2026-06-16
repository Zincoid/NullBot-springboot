package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.core.module.game.model.Match;

public abstract class GameLogic<M extends Match, S extends GameState> {

    public abstract S create(M match);
}
