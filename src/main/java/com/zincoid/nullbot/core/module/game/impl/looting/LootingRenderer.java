package com.zincoid.nullbot.core.module.game.impl.looting;

import com.zincoid.nullbot.core.module.game.impl.looting.model.LootingPlayer;
import com.zincoid.nullbot.core.module.game.framework.Renderer;
import org.springframework.stereotype.Component;

@Component
public class LootingRenderer extends Renderer<LootingState> {

    @Override
    public String render(LootingState state) {
        return state.getMap().printSpawn()
                + "\n\nTips: 可以先通过\"/摸金\"来侦察一下, 侦察不消耗Tick";
    }

    public String statusLine(LootingPlayer player, int remainingTick) {
        return """
                【玩家%s】 HP: %s💟
                [=== 🕒距迷失还剩 %s 刻 ===]"""
                .formatted(player.getUserId(), player.getHp(), remainingTick);
    }

    public String actionMenu(boolean finished, boolean canEvac, boolean hasEnemies) {
        if (finished) return "";
        StringBuilder sb = new StringBuilder("[可执行操作(格式:/摸金 [动作])]");
        sb.append("\n侦察 - 移动 [地点] - 搜刮");
        if (canEvac) sb.append(" - 撤离");
        if (hasEnemies) sb.append("\n[⚔️ ====== 攻击AI - 攻击玩家]");
        return sb.toString();
    }
}
