package org.bot.nullbot.entity.game.looting;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class LootingMap {

    private String name;
    private Map<String, MapNode> nodes = new LinkedHashMap<>();

    public MapNode node(String name) {
        return nodes.get(name);
    }

    public String print() {
        StringBuilder sb = new StringBuilder("🗺 地图: " + name);
        for (MapNode n : nodes.values()) {
            sb.append("\n").append(n.print());
        }
        return sb.toString();
    }

    public String printWithoutItems() {
        StringBuilder sb = new StringBuilder("🗺 地图: " + name);
        for (MapNode n : nodes.values()) {
            sb.append("\n").append(n.printWithoutItems());
        }
        return sb.toString();
    }

    public String printSpawn() {
        StringBuilder sb = new StringBuilder("🗺 地图: " + name);
        for (MapNode n : nodes.values()) {
            if(n.isSpawn())
                sb.append("\n").append(n.printWithoutItems());
        }
        return sb.toString();
    }
}
