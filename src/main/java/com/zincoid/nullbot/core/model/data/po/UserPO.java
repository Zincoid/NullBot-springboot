package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`user`")
public class UserPO {

    private Long  id;
    private String name;
    private Integer access;
    private Integer level;
    private Integer experience;
    private Integer cash;
    private Integer capacity;
    private Integer drawTimes;

    public UserPO(Long id, String name) {
        this.id = id;
        this.name = name;
        this.access = 0;
        this.level = 1;
        this.experience = 0;
        this.cash = 0;
        this.capacity = 100;
        this.drawTimes = 50;
    }

    public int plusExperience(int exp) {
        int upgrade = 0;
        experience += exp;
        while(experience >= getMaxExperience()){
            experience -= getMaxExperience();
            upgrade();
            upgrade++;
        }
        return upgrade;
    }

    private int getMaxExperience() {
        return 100 + (level - 1) * 10;
    }

    private void upgrade() {
        level++;
        capacity += 50;
    }

    @Override
    public String toString() {
        return String.format(
                """
                        ◆%s(%d)
                        [限权] %d 级
                        [等级] Lv.%d (Exp:%d/%d)
                        [现金] ￥%d
                        [抽数] %d
                        [库容] %d""",
                name, id, access, level, experience, getMaxExperience(), cash, drawTimes, capacity
        );
    }
}
