package com.zincoid.nullbot.core.module.game.impl.looting.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

@Data
public class LootingMap {

    private String name;
    private Map<String, MapNode> nodes = new LinkedHashMap<>();

    public MapNode node(String name) {
        return nodes.get(name);
    }

    public String print() {
        return printWithFilter(n -> true, true);
    }

    public String printWithoutItems() {
        return printWithFilter(n -> true, false);
    }

    public String printSpawn() {
        return printWithFilter(MapNode::isSpawn, false);
    }

    private String printWithFilter(Predicate<MapNode> filter, boolean showItems) {
        StringBuilder sb = new StringBuilder("🗺 地图: " + name);
        for (MapNode n : nodes.values())
            if (filter.test(n))
                sb.append("\n").append(showItems ? n.print() : n.printWithoutItems());
        return sb.toString();
    }
}
