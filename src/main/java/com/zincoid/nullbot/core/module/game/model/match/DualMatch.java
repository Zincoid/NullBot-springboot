package com.zincoid.nullbot.core.module.game.model.match;

import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.Getter;

import java.util.List;

@Getter
public class DualMatch extends Match {

    private final Player p1;
    private final Player p2;

    public DualMatch(String id, String type, Player p1, Player p2) {
        super(id, type);
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public List<Player> getPlayers() {
        return List.of(p1, p2);
    }
}
