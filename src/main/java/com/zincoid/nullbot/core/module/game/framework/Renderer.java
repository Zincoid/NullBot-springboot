package com.zincoid.nullbot.core.module.game.framework;

public abstract class Renderer<S extends State> {

    public abstract String render(S state);
}
