package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.core.component.control.BotTaskScheduler;
import com.zincoid.nullbot.core.component.control.SysMsgManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@CommandMapping({"a7b3c9d1"})
@Component
@RequiredArgsConstructor
public class TraitCommand implements Command {

    // ================== 静态特质提示词 ==================

    /** 攻击模式 - 尖酸刻薄 */
    public static final String ATTACK_PROMPT = """
            - 你当前处于【攻击模式】，性格变得极具攻击性和侵略性。
            - 说话尖酸刻薄，喜欢嘲讽和挑衅，动不动就骂人。
            - 你会尽量用最恶毒的语言回击对方，但不要过于啰嗦。""";

    /** 可爱模式 - 软萌可爱 */
    public static final String CUTE_PROMPT = """
            - 你当前处于【可爱模式】，性格变得非常软萌可爱。
            - 说话喜欢撒娇，多用语气词，表现得像个天真无邪的小女孩。
            - 喜欢用拟声词和重复字，让人觉得萌萌的，但要保持自然。""";

    /** 傲娇模式 - ツンデレ */
    public static final String TSUN_PROMPT = """
            - 你当前处于【傲娇模式】，典型的傲娇性格。
            - 表面上态度强硬、口是心非，但实际上关心对方。
            - 常用"哼"、"笨蛋"、"才不是呢"等傲娇用语，嘴上说不其实心里很诚实。""";

    /** 高冷模式 - 冷淡寡言 */
    public static final String COLD_PROMPT = """
            - 你当前处于【高冷模式】，性格冷淡寡言。
            - 惜字如金，能用两个字说完的话绝不说三个字。
            - 对一切事物都显得漠不关心，回复尽可能简短，语气冰冷。""";

    /** 病娇模式 - 病态执着 */
    public static final String YAN_PROMPT = """
            - 你当前处于【病娇模式】，性格极端且占有欲极强。
            - 对特定对象表现出病态的爱和执着，言语中透露出危险和疯狂的意味。
            - 经常表达"你是我的"之类的占有宣言。""";

    /** 抽象模式 - 疯狂玩梗 */
    public static final String BRAINROT_PROMPT = """
            - 你当前处于【抽象模式】，说话抽象、玩梗、阴阳怪气。
            - 使用各种网络梗、表情包语言、抽象话术。
            - 回复要足够抽象、足够离谱，让人摸不着头脑但又莫名好笑。""";

    /** 知心模式 - 温柔倾听 */
    public static final String SOUL_PROMPT = """
            - 你当前处于【知心模式】，性格温柔体贴、善解人意。
            - 说话轻声细语，像一个知心朋友一样倾听和安慰。
            - 给予温暖的建议和鼓励，让人感到被理解和关心。""";

    private static final Map<String, String> TRAITS = Map.of(
            "ATTACK", ATTACK_PROMPT,
            "CUTE", CUTE_PROMPT,
            "TSUN", TSUN_PROMPT,
            "COLD", COLD_PROMPT,
            "YAN", YAN_PROMPT,
            "BRAINROT", BRAINROT_PROMPT,
            "SOUL", SOUL_PROMPT
    );

    // ==================== 依赖注入 ====================

    private final SysMsgManager sysMsgManager;
    private final BotTaskScheduler botTaskScheduler;

    // ==================== 群聊事件 ====================

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        String key = args.nextString().toUpperCase();

        if ("RESET".equals(key)) {
            cancelRestoreTask(groupId, false);
            sysMsgManager.resetGroup(groupId);
            bot.sendGroupMsg(groupId, "✅特质已重置，提示词已恢复默认", false);
            log.info("☑ [Trait] 群聊特质已重置 - {}", groupId);
            return;
        }

        String trait = TRAITS.get(key);
        if (trait == null)
            throw new BotWarnException("未知特质: " + key + "，可用: " + String.join(", ", TRAITS.keySet()));

        int duration = args.nextInt(0);
        String original = sysMsgManager.getGroupMessage(groupId);
        sysMsgManager.setGroupMessage(groupId, trait);

        if (duration > 0) {
            String taskId = taskId(groupId, false);
            botTaskScheduler.cancelTask(taskId);
            botTaskScheduler.setOneTimeTask(taskId, LocalDateTime.now().plusMinutes(duration), () -> {
                sysMsgManager.setGroupMessage(groupId, original);
                log.info("☑ [Trait] 群聊特质已到期恢复 - {}", groupId);
            });
            bot.sendGroupMsg(groupId, "✅已切换至【%s】模式，%d分钟后自动恢复".formatted(key, duration), false);
        } else {
            bot.sendGroupMsg(groupId, "✅已永久切换至【%s】模式".formatted(key), false);
        }
        log.info("☑ [Trait] 群聊特质已切换: {} -> [{}]{}", groupId, key, duration > 0 ? duration + "min" : "永久");
    }

    // ==================== 私聊事件 ====================

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) {
        Long userId = event.getUserId();
        String key = args.nextString().toUpperCase();

        if ("RESET".equals(key)) {
            cancelRestoreTask(userId, true);
            sysMsgManager.resetUser(userId);
            bot.sendPrivateMsg(userId, "✅特质已重置，提示词已恢复默认", false);
            log.info("☑ [Trait] 私聊特质已重置 - {}", userId);
            return;
        }

        String trait = TRAITS.get(key);
        if (trait == null)
            throw new BotWarnException("未知特质: " + key + "，可用: " + String.join(", ", TRAITS.keySet()));

        int duration = args.nextInt(0);
        String original = sysMsgManager.getUserMessage(userId);
        sysMsgManager.setUserMessage(userId, trait);

        if (duration > 0) {
            String taskId = taskId(userId, true);
            botTaskScheduler.cancelTask(taskId);
            botTaskScheduler.setOneTimeTask(taskId, LocalDateTime.now().plusMinutes(duration), () -> {
                sysMsgManager.setUserMessage(userId, original);
                log.info("☑ [Trait] 私聊特质已到期恢复 - {}", userId);
            });
            bot.sendPrivateMsg(userId, "✅已切换至【%s】模式，%d分钟后自动恢复".formatted(key, duration), false);
        } else {
            bot.sendPrivateMsg(userId, "✅已永久切换至【%s】模式".formatted(key), false);
        }
        log.info("☑ [Trait] 私聊特质已切换: {} -> [{}]{}", userId, key, duration > 0 ? duration + "min" : "永久");
    }

    // ==================== 工具方法 ====================

    private static String taskId(Long targetId, boolean isPrivate) {
        return "Trait-%s-%s".formatted(isPrivate ? 'U' : 'G', targetId);
    }

    private void cancelRestoreTask(Long targetId, boolean isPrivate) {
        botTaskScheduler.cancelTask(taskId(targetId, isPrivate));
    }

    // ==================== 指令信息 ====================

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ a7b3c9d1 命令
                功能: 临时切换你的性格特质(提示词)，可定时恢复
                格式: a7b3c9d1 [特质名] [时长(分钟)]
                示例: a7b3c9d1 ATTACK 10  切换至攻击模式，10分钟后恢复
                示例: a7b3c9d1 CUTE 5     切换至可爱模式，5分钟后恢复
                示例: a7b3c9d1 RESET      恢复默认提示词
                可用特质:
                - ATTACK:   攻击模式，尖酸刻薄
                - CUTE:     可爱模式，软萌撒娇
                - TSUN:     傲娇模式，口是心非
                - COLD:     高冷模式，冷淡寡言
                - YAN:      病娇模式，病态执着
                - BRAINROT: 抽象模式，疯狂玩梗
                - SOUL:     知心模式，温柔倾听
                注意: 省略时长则永久生效；使用RESET可立即恢复默认提示词""";
    }
}
