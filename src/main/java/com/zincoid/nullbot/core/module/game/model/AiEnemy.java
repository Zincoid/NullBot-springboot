package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;
import com.zincoid.nullbot.core.model.data.po.ItemPO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class AiEnemy {

    private String name;
    private String location;
    private int hp = 50;
    private int atk = 10;

    private List<ItemPO> backpack = new ArrayList<>();

    public boolean alive() {
        return hp > 0;
    }

    public int getAtk() {
        return atk + ThreadLocalRandom.current().nextInt(atk);
    }
}
