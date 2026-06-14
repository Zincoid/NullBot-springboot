package com.zincoid.nullbot.core.module.game.factory;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.module.game.model.LootingMap;
import com.zincoid.nullbot.core.module.game.model.MapNode;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.basic.ItemService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class LootingMapFactory {

    private final ItemService itemService;

    public LootingMap randomMap() {
        return switch (ThreadLocalRandom.current().nextInt(2)) {
            case 0 -> lab();
            case 1 -> spaceport();
            default -> throw new IllegalStateException();
        };
    }

    private LootingMap lab() {
        LootingMap m = new LootingMap();
        m.setName("研究所");
        m.getNodes().put("入口", node("入口", true, false, false, List.of("走廊")));
        m.getNodes().put("密道", node("密道", true, false, false, List.of("走廊")));
        m.getNodes().put("走廊", node("走廊", false, false, false, List.of("入口","密道","仓库","实验室")));
        m.getNodes().put("仓库", node("仓库", false, false, false, List.of("走廊","停机坪")));
        m.getNodes().put("实验室", node("实验室", false, true, false, List.of("走廊","应急通道")));
        m.getNodes().put("停机坪", node("停机坪", false, false, true, List.of("仓库")));
        m.getNodes().put("应急通道", node("应急通道", false, false, true, List.of("实验室")));
        return m;
    }

    private LootingMap spaceport() {
        LootingMap m = new LootingMap();
        m.setName("航天基地");

        // ===== 出生点 =====
        m.getNodes().put("宿舍", node("宿舍", true, false, false, List.of("中控室", "西区大门")));
        m.getNodes().put("西区大门", node("西区大门", true, false, false, List.of("宿舍", "浮力室", "离心室")));
        m.getNodes().put("运输通道", node("运输通道", true, false, false, List.of("发射台", "停机坪")));
        m.getNodes().put("东区吊桥", node("东区吊桥", true, false, false, List.of("罐装区", "黑室", "水平试车间")));
        m.getNodes().put("工业区", node("工业区", true, false, false, List.of("中控室", "罐装区")));
        // ===== 核心区域 =====
        m.getNodes().put("离心室", node("离心室", false, true, false, List.of("西区大门", "浮力室", "停机坪")));
        m.getNodes().put("浮力室", node("浮力室", false, true, false, List.of("西区大门", "中控桥", "离心室", "总裁室")));
        m.getNodes().put("蓝室", node("蓝室", false, true, false, List.of("中控桥", "黑室")));
        m.getNodes().put("黑室", node("黑室", false, true, false, List.of("总裁室", "蓝室", "东区吊桥", "停机坪")));
        m.getNodes().put("总裁室", node("总裁室", false, true, false, List.of("浮力室", "黑室")));
        m.getNodes().put("发射台", node("发射台", false, true, false, List.of("运输通道", "火箭")));
        // ===== 中央通道 =====
        m.getNodes().put("中控室", node("中控室", false, true, false, List.of("宿舍", "工业区", "中控桥")));
        m.getNodes().put("中控桥", node("中控桥", false, false, false, List.of("中控室", "浮力室", "蓝室")));
        // ===== 工业区=====
        m.getNodes().put("罐装区", node("罐装区", false, false, false, List.of("工业区", "东区吊桥")));
        m.getNodes().put("水平试车间", node("水平试车间", false, false, false, List.of("停机坪", "东区吊桥", "试车台")));
        // ===== 撤离点 =====
        m.getNodes().put("停机坪", node("停机坪", false, false, true, List.of("离心室", "运输通道", "黑室", "水平试车间")));
        m.getNodes().put("试车台", node("试车台", false, false, true, List.of("水平试车间")));
        m.getNodes().put("火箭", node("火箭", false, false, true, List.of("发射台")));

        return m;
    }

    private MapNode node(String name, boolean s, boolean h, boolean e, List<String> n) {
        return new MapNode(name, s, h, e, n, randItems(h));
    }

    public List<ItemPO> randItems(boolean highValue) {
        int n = 1 + ThreadLocalRandom.current().nextInt(3);
        List<ItemPO> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if(highValue)
                list.add(itemService.getRandomHighValue());
            else
                list.add(itemService.getRandom());
        }
        return list;
    }
}
