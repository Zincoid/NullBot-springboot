package com.zincoid.nullbot.core.model.game.looting;

import lombok.Data;
import com.zincoid.nullbot.core.model.po.ItemPO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Random rand = new Random();
        return atk + rand.nextInt(atk);
    }
}
