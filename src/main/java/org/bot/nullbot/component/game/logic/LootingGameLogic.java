package org.bot.nullbot.component.game.logic;

import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.game.GameLogic;
import org.bot.nullbot.component.game.factory.LootingMapFactory;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.looting.*;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.util.game.DamageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class LootingGameLogic extends GameLogic
{
    @Value("${nullbot.bot-id}")
    Long botId;
    private final BotContainer botContainer;
    private final LootingMapFactory mapFactory;
    private static final Random R = new Random();

    public LootingGameState create(Match match) {
        LootingGameState s = new LootingGameState();
        s.setMap(mapFactory.randomMap());

        List<String> spawns = s.getMap().getNodes().values()
                .stream().filter(MapNode::isSpawn)
                .map(MapNode::getName).toList();

        s.getPlayers().put(match.getPlayer1().getUserId(),
                new LootingPlayerState(match.getPlayer1().getUserId(), spawns.get(0)));
        s.getPlayers().put(match.getPlayer2().getUserId(),
                new LootingPlayerState(match.getPlayer2().getUserId(), spawns.get(1)));

        initAi(s);
        return s;
    }

    private void initAi(LootingGameState s) {
        int n = 1 + R.nextInt(2);
        List<String> nodes = new ArrayList<>(s.getMap().getNodes().keySet());

        for (int i = 0; i < n; i++) {
            AiEnemyState ai = new AiEnemyState();
            ai.setName("AI-" + (i + 1));
            ai.setLocation(nodes.get(R.nextInt(nodes.size())));
            ai.getBackpack().addAll(mapFactory.randItems());
            s.getEnemies().add(ai);
        }
    }

    public String tick(LootingGameState s, Long selfId, Long opponentGroupId) {
        s.setTick(s.getTick() + 1);
        checkFinished(s);
        return aiAction(s, selfId, opponentGroupId);
    }

    public void checkFinished(LootingGameState s) {
        boolean isFinished = true;
        if (s.getTick() <= 25) {
            for(LootingPlayerState p : s.getPlayers().values()){
                if (p.isAlive() && !p.isEvacuated()) {
                    isFinished = false;
                    break;
                }
            }
        }
        s.setFinished(isFinished);
    }

    public String checkEnemies(LootingGameState s, LootingPlayerState p) {
        StringBuilder sb = new StringBuilder();
        for (AiEnemyState ai : s.getEnemies()) {
            if (ai.alive() && ai.getLocation().equals(p.getLocation())) {
                sb.append("\n⚠️ 发现AI敌人: ")
                        .append(ai.getName())
                        .append(" HP ").append(ai.getHp());
            }
        }

        for (LootingPlayerState other : s.getPlayers().values()) {
            if (other != p && other.isAlive() && other.getLocation().equals(p.getLocation())) {
                sb.append("\n⚠️ 发现玩家: ")
                        .append(other.getUserId())
                        .append(" HP ").append(other.getHp());
            }
        }
        return sb.toString();
    }

    // ===== 移动 / 侦察 / 搜刮 / 攻击 / AI 行为 / 撤离 / 下一步 =====

    public String move(LootingGameState s, LootingPlayerState p, String target) {
        MapNode cur = s.getMap().node(p.getLocation());
        if (!cur.getNeighbors().contains(target)) {
            return "\n❌ 无法移动到该位置";
        }
        p.setLocation(target);
        return "\n🚶 你移动到了 " + target + "\n" +
                s.getMap().node(target).printWithoutItems();
    }

    public String view(LootingGameState s, LootingPlayerState p) {
        StringBuilder sb = new StringBuilder();
        MapNode node = s.getMap().node(p.getLocation());
        sb.append(node.print());
        return sb.toString();
    }

    public String loot(LootingGameState s, LootingPlayerState p) {
        MapNode node = s.getMap().node(p.getLocation());
        if (node.getItems().isEmpty()) {
            return "\n📦 这里没有可以搜刮的物品";
        }

        StringBuilder sb = new StringBuilder("\n📦 你搜刮到了: ");
        for (ItemPO i : node.getItems()) {
            p.getBackpack().add(i);
            sb.append("\n- ").append(i.getName())
                    .append(" (").append(i.getRarity()).append(")");
        }
        node.getItems().clear();
        return sb.toString();
    }

    public String attackAi(LootingGameState s, LootingPlayerState p) {
        for (AiEnemyState ai : s.getEnemies()) {
            if (ai.alive() && ai.getLocation().equals(p.getLocation())) {
                int dmg = DamageUtil.playerDamage();
                ai.setHp(Math.max(ai.getHp() - dmg, 0));

                StringBuilder sb = new StringBuilder();
                sb.append("\n⚔️ 你对").append(ai.getName())
                        .append("造成").append(dmg).append("伤害\n")
                        .append("对方HP：").append(ai.getHp());

                if (!ai.alive()) {
                    sb.append("\n💀 已击败").append(ai.getName()).append(",获得他的战利品:");
                    for (ItemPO i : ai.getBackpack()) {
                        p.getBackpack().add(i);
                        sb.append("\n🎁 ").append(i.getName()).append(" (").append(i.getRarity()).append(")");
                    }
                }
                return sb.toString();
            }
        }
        return "\n❌ 当前位置没有 AI 敌人";
    }

    public String attackPlayer(LootingGameState s, LootingPlayerState p, Long opponentGroupId) {
        for (LootingPlayerState other : s.getPlayers().values()) {
            if (other != p && other.isAlive() && other.getLocation().equals(p.getLocation())) {
                int dmg = DamageUtil.playerDamage();
                other.setHp(Math.max(other.getHp() - dmg, 0));

                StringBuilder sb = new StringBuilder();
                sb.append("\n⚔️ 你对玩家").append(other.getUserId())
                        .append("造成").append(dmg).append("伤害\n")
                        .append("对方HP: ").append(other.getHp());

                if (other.getHp() <= 0) {
                    other.setAlive(false);
                    sb.append("\n💀 已击败").append(other.getUserId()).append(",获得他的战利品:");
                    for (ItemPO i : other.getBackpack()) {
                        p.getBackpack().add(i);
                        sb.append("\n🎁 ").append(i.getName()).append(" (").append(i.getRarity()).append(")");
                    }
                }
                botContainer.robots.get(botId).sendGroupMsg(opponentGroupId, "⚔️ 有玩家攻击了" + other.getUserId() +"！剩余HP: " + other.getHp(), false);
                return sb.toString();
            }
        }
        return "\n❌ 当前位置没有可攻击的玩家";
    }

    private String aiAction(LootingGameState s, Long selfId, Long opponentGroupId) {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (AiEnemyState ai : s.getEnemies()) {
            if (!ai.alive()) continue;
            if(R.nextBoolean()){
                // 攻击同节点玩家
                for (LootingPlayerState p : s.getPlayers().values()) {
                    if (p.isAlive() && p.getLocation().equals(ai.getLocation())) {
                        if(Objects.equals(p.getUserId(), selfId)){
                            int dmg = DamageUtil.aiDamage();
                            p.setHp(p.getHp() - dmg);
                            sb1.append("\n⚔️ 敌人").append(ai.getName()).append("对你造成").append(dmg).append("伤害\n");
                            if (p.getHp() <= 0) {
                                p.setAlive(false);
                                ai.getBackpack().addAll(p.getBackpack());
                                p.getBackpack().clear();
                                sb1.append("\n 💀 你死了");
                            }
                        }else{
                            int dmg = DamageUtil.aiDamage();
                            p.setHp(p.getHp() - dmg);
                            sb2.append("⚔️ 敌人").append(ai.getName()).append("对玩家").append(p.getUserId()).append("造成").append(dmg).append("伤害\n").append("玩家剩余HP: ").append(p.getHp());
                            if (p.getHp() <= 0) {
                                p.setAlive(false);
                                ai.getBackpack().addAll(p.getBackpack());
                                p.getBackpack().clear();
                                sb2.append("\n 💀 你死了");
                            }
                        }
                    }
                }
            }else{
                // 移动
                MapNode node = s.getMap().node(ai.getLocation());
                if (!node.getNeighbors().isEmpty()) {
                    ai.setLocation(node.getNeighbors()
                            .get(R.nextInt(node.getNeighbors().size())));
                }
            }
        }
        if(!sb2.isEmpty())
            botContainer.robots.get(botId).sendGroupMsg(opponentGroupId, sb2.toString(), false);
        return sb1.toString();
    }

    public String evac(LootingGameState s, LootingPlayerState p) {
        MapNode node = s.getMap().node(p.getLocation());
        if (!node.isEvac()) {
            return "\n❌ 这里不是撤离点";
        }
        p.setEvacuated(true);

        StringBuilder sb = new StringBuilder("\n🚪 已成功撤离！\n🎒 带出物品: ");
        for (ItemPO i : p.getBackpack()) {
            sb.append("\n").append(i.getName()).append(" (").append(i.getRarity()).append(")");
        }
        return sb.toString();
    }
}
