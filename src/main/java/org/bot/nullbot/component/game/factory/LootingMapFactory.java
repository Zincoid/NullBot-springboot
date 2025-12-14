package org.bot.nullbot.component.game.factory;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.game.looting.LootingMap;
import org.bot.nullbot.entity.game.looting.MapNode;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.service.ItemService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class LootingMapFactory {

    private final ItemService itemService;
    private static final Random R = new Random();

    public LootingMap randomMap() {
        return R.nextBoolean() ? lab() : factory();
    }

    private LootingMap lab() {
        LootingMap m = new LootingMap();
        m.setName("废弃研究所");

        m.getNodes().put("出生点A", node("出生点A", true, false, false, List.of("走廊")));
        m.getNodes().put("出生点B", node("出生点B", true, false, false, List.of("走廊")));
        m.getNodes().put("走廊", node("走廊", false, false, false,
                List.of("出生点A","出生点B","仓库","实验室")));
        m.getNodes().put("仓库", node("仓库", false, false, false,
                List.of("走廊","撤离点1")));
        m.getNodes().put("实验室", node("实验室", false, true, false,
                List.of("走廊","撤离点2")));
        m.getNodes().put("撤离点1", node("撤离点1", false, false, true, List.of("仓库")));
        m.getNodes().put("撤离点2", node("撤离点2", false, false, true, List.of("实验室")));
        return m;
    }

    private LootingMap factory() {
        LootingMap m = new LootingMap();
        m.setName("旧工厂");

        m.getNodes().put("出生点A", node("出生点A", true, false, false, List.of("车间")));
        m.getNodes().put("出生点B", node("出生点B", true, false, false, List.of("车间")));
        m.getNodes().put("车间", node("车间", false, false, false,
                List.of("出生点A","出生点B","办公室","库房")));
        m.getNodes().put("办公室", node("办公室", false, true, false,
                List.of("车间","撤离点1")));
        m.getNodes().put("库房", node("库房", false, false, false,
                List.of("车间","撤离点2")));
        m.getNodes().put("撤离点1", node("撤离点1", false, false, true, List.of("办公室")));
        m.getNodes().put("撤离点2", node("撤离点2", false, false, true, List.of("库房")));
        return m;
    }

    private MapNode node(String name, boolean s, boolean h, boolean e, List<String> n) {
        return new MapNode(name, s, h, e, n, randItems());
    }

    public List<ItemPO> randItems() {
        int n = 1 + R.nextInt(3);
        List<ItemPO> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(itemService.getRandomItem());
        }
        return list;
    }
}
