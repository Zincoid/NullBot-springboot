package com.zincoid.nullbot.core.module.game.impl.looting;

import com.zincoid.nullbot.core.module.game.framework.GameLogic;
import com.zincoid.nullbot.core.module.game.impl.looting.model.AiEnemy;
import com.zincoid.nullbot.core.module.game.impl.looting.model.LootingPlayer;
import com.zincoid.nullbot.core.module.game.impl.looting.model.MapNode;
import com.zincoid.nullbot.core.module.game.model.DualMatch;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class LootingLogic extends GameLogic<DualMatch, LootingState> {

    private final LootingMapFactory mapFactory;

    @Override
    public LootingState create(DualMatch match) {
        LootingState s = new LootingState();
        s.setMap(mapFactory.randomMap());

        List<String> spawns = s.getMap().getNodes().values()
                .stream().filter(MapNode::isSpawn)
                .map(MapNode::getName).toList();

        s.getPlayers().put(match.getP1().getId(),
                new LootingPlayer(match.getP1().getId(), spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()))));
        s.getPlayers().put(match.getP2().getId(),
                new LootingPlayer(match.getP2().getId(), spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()))));

        initAi(s);
        return s;
    }

    private void initAi(LootingState s) {
        int n = 1 + ThreadLocalRandom.current().nextInt(3);
        List<String> nodes = new ArrayList<>(s.getMap().getNodes().keySet());

        // 定制 AI
        if ("航天基地".equals(s.getMap().getName())) {
            AiEnemy ai = new AiEnemy();
            ai.setName("德穆兰");
            ai.setHp(100);
            ai.setAtk(20);
            ai.setLocation(nodes.get(ThreadLocalRandom.current().nextInt(nodes.size())));
            ai.getBackpack().addAll(mapFactory.randItems(true));
            s.getEnemies().add(ai);
        }

        // 通用 AI
        for (int i = 0; i < n; i++) {
            AiEnemy ai = new AiEnemy();
            ai.setName("士兵-" + (i + 1));
            ai.setLocation(nodes.get(ThreadLocalRandom.current().nextInt(nodes.size())));
            ai.getBackpack().addAll(mapFactory.randItems(false));
            s.getEnemies().add(ai);
        }
    }

    public void checkFinished(LootingState s) {
        boolean isFinished = true;
        if (s.getTick() <= 25) {
            for (LootingPlayer p : s.getPlayers().values()) {
                if (p.isAlive() && !p.isEvacuated()) {
                    isFinished = false;
                    break;
                }
            }
        }
        s.setFinished(isFinished);
    }

    public String checkEnemies(LootingState s, LootingPlayer p) {
        StringBuilder sb = new StringBuilder();
        for (AiEnemy ai : s.getEnemies())
            if (ai.alive() && ai.getLocation().equals(p.getLocation()))
                sb.append("\n⚠️发现AI敌人: ").append(ai.getName());
        for (LootingPlayer other : s.getPlayers().values())
            if (other != p && other.isAlive() && !other.isEvacuated() && other.getLocation().equals(p.getLocation()))
                sb.append("\n⚠️发现玩家: ").append(other.getUserId());
        return sb.toString();
    }

    // ===== 推进游戏刻 =====

    public List<String> tick(LootingState s, Long selfId) {
        s.setTick(s.getTick() + 1);
        checkFinished(s);
        return aiAction(s, selfId);
    }

    // ===== 移动 / 侦察 / 搜刮 / 攻击 / AI行为 / 撤离 =====

    public String move(LootingState s, LootingPlayer p, String target) {
        MapNode cur = s.getMap().node(p.getLocation());
        if (!cur.getNeighbors().contains(target)) {
            return "\n❌无法移动至此";
        }
        p.setLocation(target);
        return "\n▶️你移动到了 " + target + "\n" +
                s.getMap().node(target).printWithoutItems();
    }

    public String view(LootingState s, LootingPlayer p) {
        return "\n" + s.getMap().node(p.getLocation()).print();
    }

    public String loot(LootingState s, LootingPlayer p) {
        MapNode node = s.getMap().node(p.getLocation());
        if (node.getItems().isEmpty()) {
            return "\n📦无可搜刮物品";
        }

        StringBuilder sb = new StringBuilder("\n📦你搜刮到了: ");
        for (ItemPO i : node.getItems()) {
            p.getBackpack().add(i);
            sb.append("\n- ").append(i.getName())
                    .append(" (").append(i.getRarity().getDescription()).append(")");
        }
        node.getItems().clear();
        return sb.toString();
    }

    public String attackAi(LootingState s, LootingPlayer p) {
        for (AiEnemy ai : s.getEnemies()) {
            if (ai.alive() && ai.getLocation().equals(p.getLocation())) {
                int dmg = p.getAtk();
                ai.setHp(Math.max(ai.getHp() - dmg, 0));

                StringBuilder sb = new StringBuilder();
                sb.append("\n⚔️你对").append(ai.getName())
                        .append("造成").append(dmg).append("伤害\n")
                        .append("对方剩余HP：").append(ai.getHp());

                if (!ai.alive()) {
                    sb.append("\n💀已击败").append(ai.getName()).append(" 获得他的战利品:");
                    for (ItemPO i : ai.getBackpack()) {
                        p.getBackpack().add(i);
                        sb.append("\n🎁").append(i.getName()).append(" (").append(i.getRarity().getDescription()).append(")");
                    }
                }
                return sb.toString();
            }
        }
        return "\n❌未找到AI敌人";
    }

    public List<String> attackPlayer(LootingState s, LootingPlayer p) {
        for (LootingPlayer other : s.getPlayers().values()) {
            if (other != p && other.isAlive() && !other.isEvacuated() && other.getLocation().equals(p.getLocation())) {
                int dmg = p.getAtk();
                other.setHp(Math.max(other.getHp() - dmg, 0));

                StringBuilder sb = new StringBuilder();
                sb.append("\n⚔️你对玩家").append(other.getUserId())
                        .append("造成").append(dmg).append("伤害\n")
                        .append("对方剩余HP: ").append(other.getHp());

                if (other.getHp() <= 0) {
                    other.setAlive(false);
                    sb.append("\n💀已击败").append(other.getUserId()).append(" 获得他的战利品:");
                    for (ItemPO i : other.getBackpack()) {
                        p.getBackpack().add(i);
                        sb.append("\n🎁").append(i.getName()).append(" (").append(i.getRarity().getDescription()).append(")");
                    }
                }
                return List.of(sb.toString(), "⚔️玩家攻击了" + other.getUserId() + " 造成" + dmg + "伤害 对方剩余HP: " + other.getHp());
            }
        }
        return List.of("\n❌未找到玩家", "");
    }

    private List<String> aiAction(LootingState s, Long selfId) {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (AiEnemy ai : s.getEnemies()) {
            if (!ai.alive()) continue;
            if (ThreadLocalRandom.current().nextBoolean()) {
                for (LootingPlayer p : s.getPlayers().values()) {
                    if (!p.isAlive() || !p.getLocation().equals(ai.getLocation())) continue;
                    boolean self = Objects.equals(p.getUserId(), selfId);
                    int dmg = ai.getAtk();
                    p.setHp(p.getHp() - dmg);
                    StringBuilder sb = self ? sb1 : sb2;
                    if (self)
                        sb.append("\n⚔️").append(ai.getName()).append("对你造成").append(dmg).append("伤害\n");
                    else
                        sb.append("⚔️").append(ai.getName()).append("攻击了").append(p.getUserId()).append(",造成").append(dmg).append("伤害 ").append("你的剩余HP: ").append(p.getHp());
                    if (p.getHp() <= 0) {
                        p.setAlive(false);
                        ai.getBackpack().addAll(p.getBackpack());
                        p.getBackpack().clear();
                        sb.append("\n💀你死了");
                    }
                }
            } else {
                MapNode node = s.getMap().node(ai.getLocation());
                if (!node.getNeighbors().isEmpty())
                    ai.setLocation(node.getNeighbors().get(ThreadLocalRandom.current().nextInt(node.getNeighbors().size())));
            }
        }
        return List.of(sb1.toString(), sb2.toString());
    }

    public String evac(LootingState s, LootingPlayer p) {
        MapNode node = s.getMap().node(p.getLocation());
        if (!node.isEvac()) {
            return "\n❌非撤离点";
        }
        p.setEvacuated(true);

        StringBuilder sb = new StringBuilder("\n🚪已成功撤离\n🎒带出物品: ");
        for (ItemPO i : p.getBackpack())
            sb.append("\n").append(i.getName()).append("(").append(i.getRarity().getDescription()).append(")");
        return sb.toString();
    }
}
