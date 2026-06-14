package com.zincoid.nullbot.core.module.game.logic;

import com.zincoid.nullbot.core.module.game.state.GameState;
import com.zincoid.nullbot.core.module.game.model.Match;

public abstract class GameLogic<S extends GameState> {

    public abstract S create(Match match);
}
