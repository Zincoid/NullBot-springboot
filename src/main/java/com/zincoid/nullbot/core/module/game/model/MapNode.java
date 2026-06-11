package com.zincoid.nullbot.core.module.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.ItemPO;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapNode {

    private String name;
    private boolean spawn;
    private boolean highValue;
    private boolean evac;

    private List<String> neighbors = new ArrayList<>();
    private List<ItemPO> items = new ArrayList<>();

    public String print() {
        StringBuilder sb = new StringBuilder();
        if (spawn) sb.append("[\uD83C\uDFC1出生点] ");
        if (highValue) sb.append("[💎高价值] ");
        if (evac) sb.append("[🚪撤离点] ");

        sb.append(name).append("\n◉ 可移动至: \n")
                .append(neighbors.isEmpty() ? "无" : String.join("/", neighbors));

        if (!items.isEmpty()) {
            sb.append("\n◉ 地面物品: ");
            for (ItemPO i : items) {
                sb.append("\n- ").append(i.getName())
                        .append(" (").append(i.getRarity().getDescription()).append(")");
            }
        } else {
            sb.append("\n◉ 地面物品: 无");
        }
        return sb.toString();
    }

    public String printWithoutItems() {
        StringBuilder sb = new StringBuilder();
        if (spawn) sb.append("[\uD83C\uDFC1出生点] ");
        if (highValue) sb.append("[💎高价值] ");
        if (evac) sb.append("[🚪撤离点] ");

        sb.append(name).append("\n◉ 可移动至: \n")
                .append(neighbors.isEmpty() ? "无" : String.join("/", neighbors));
        return sb.toString();
    }
}
