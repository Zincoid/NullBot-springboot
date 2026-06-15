package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.core.module.game.framework.GameHandler;
import com.zincoid.nullbot.core.module.game.framework.GameLogic;
import com.zincoid.nullbot.core.module.game.framework.GameRenderer;
import com.zincoid.nullbot.core.module.game.framework.GameState;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HandlerRegistry {

    private final Map<String, GameHandler<?, ?, ?>> handlerMap = new ConcurrentHashMap<>();

    public HandlerRegistry(List<GameHandler<?, ?, ?>> handlers) {
        handlers.forEach(h -> handlerMap.put(h.getType(), h));
    }

    @SuppressWarnings("unchecked")
    public <S extends GameState, L extends GameLogic<S>, R extends GameRenderer<S>> GameHandler<S, L, R> get(String type) {
        return (GameHandler<S, L, R>) handlerMap.get(type);
    }

    public Collection<GameHandler<?, ?, ?>> getAll() {
        return handlerMap.values();
    }
}
