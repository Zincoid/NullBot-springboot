package com.zincoid.nullbot.core.module.game.framework;

public abstract class GameRenderer<S extends GameState> {

    public abstract String render(S state);
}
