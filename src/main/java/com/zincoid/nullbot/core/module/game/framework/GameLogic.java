package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.core.module.game.model.Match;

public abstract class GameLogic<S extends GameState> {

    public abstract S create(Match match);
}
