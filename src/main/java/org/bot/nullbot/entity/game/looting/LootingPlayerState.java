package org.bot.nullbot.entity.game.looting;

import lombok.Data;
import org.bot.nullbot.entity.po.ItemPO;

import java.util.ArrayList;
import java.util.List;

@Data
public class LootingPlayerState
{
    private Long userId;
    private String location;

    private int hp = 100;
    private boolean alive = true;
    private boolean evacuated = false;

    private List<ItemPO> backpack = new ArrayList<>();

    public LootingPlayerState(Long userId, String spawn) {
        this.userId = userId;
        this.location = spawn;
    }
}
