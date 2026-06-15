package com.zincoid.nullbot.core.module.game.impl.looting.model;

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
        return printBase() + printItems();
    }

    public String printWithoutItems() {
        return printBase();
    }

    private String printBase() {
        StringBuilder sb = new StringBuilder();
        if (spawn) sb.append("[🏁出生点] ");
        if (highValue) sb.append("[💎高价值] ");
        if (evac) sb.append("[🚪撤离点] ");
        sb.append(name).append("\n◉ 可移动至: \n")
                .append(neighbors.isEmpty() ? "无" : String.join("/", neighbors));
        return sb.toString();
    }

    private String printItems() {
        if (items.isEmpty()) return "\n◉ 地面物品: 无";
        StringBuilder sb = new StringBuilder("\n◉ 地面物品: ");
        for (ItemPO i : items)
            sb.append("\n- ").append(i.getName()).append(" (").append(i.getRarity().getDescription()).append(")");
        return sb.toString();
    }

}
