package com.zincoid.nullbot.core.module.game;

import com.zincoid.nullbot.core.module.game.handler.GameMatchHandler;
import com.zincoid.nullbot.core.module.game.logic.GameLogic;
import com.zincoid.nullbot.core.module.game.state.GameState;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HandlerRegistry {

    private final Map<String, GameMatchHandler<?, ?>> handlerMap = new ConcurrentHashMap<>();

    public HandlerRegistry(List<GameMatchHandler<?, ?>> handlers) {
        handlers.forEach(h -> handlerMap.put(h.gameType(), h));
    }

    @SuppressWarnings("unchecked")
    public <S extends GameState, L extends GameLogic<S>> GameMatchHandler<S, L> get(String gameType) {
        return (GameMatchHandler<S, L>) handlerMap.get(gameType);
    }

    public Collection<GameMatchHandler<?, ?>> getAll() {
        return handlerMap.values();
    }
}
