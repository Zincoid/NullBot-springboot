package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.core.module.game.framework.Handler;
import com.zincoid.nullbot.core.module.game.framework.Logic;
import com.zincoid.nullbot.core.module.game.framework.Renderer;
import com.zincoid.nullbot.core.module.game.framework.State;
import com.zincoid.nullbot.core.module.game.model.Match;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HandlerRegistry {

    private final Map<String, Handler<?, ?, ?, ?>> handlerMap = new ConcurrentHashMap<>();

    public HandlerRegistry(List<Handler<?, ?, ?, ?>> handlers) {
        handlers.forEach(h -> handlerMap.put(h.getType(), h));
    }

    @SuppressWarnings("unchecked")
    public <M extends Match, S extends State, L extends Logic<M, S>, R extends Renderer<S>> Handler<M, S, L, R> get(String type) {
        return (Handler<M, S, L, R>) handlerMap.get(type);
    }

    public Collection<Handler<?, ?, ?, ?>> getAll() {
        return handlerMap.values();
    }
}
