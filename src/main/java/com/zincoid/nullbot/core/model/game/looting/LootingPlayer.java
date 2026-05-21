package com.zincoid.nullbot.core.model.game.looting;

import lombok.Data;
import com.zincoid.nullbot.core.model.po.ItemPO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class LootingPlayer {

    private Long userId;
    private String location;

    private int hp = 100;
    private int atk = 30;
    private boolean alive = true;
    private boolean evacuated = false;

    private List<ItemPO> backpack = new ArrayList<>();

    public LootingPlayer(Long userId, String spawn) {
        this.userId = userId;
        this.location = spawn;
    }

    public int getAtk() {
        Random rand = new Random();
        return atk + rand.nextInt(atk);
    }
}
