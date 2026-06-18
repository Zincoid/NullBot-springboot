package com.zincoid.nullbot.core.module.game.framework;

import com.zincoid.nullbot.core.module.game.model.Match;

public abstract class Logic<M extends Match, S extends State> {

    public abstract S create(M match);
}
