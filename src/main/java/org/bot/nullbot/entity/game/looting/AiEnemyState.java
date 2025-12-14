package org.bot.nullbot.entity.game.looting;

import lombok.Data;
import org.bot.nullbot.entity.po.ItemPO;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiEnemyState
{
    private String name;
    private String location;
    private int hp = 50;

    private List<ItemPO> backpack = new ArrayList<>();

    public boolean alive() {
        return hp > 0;
    }
}
