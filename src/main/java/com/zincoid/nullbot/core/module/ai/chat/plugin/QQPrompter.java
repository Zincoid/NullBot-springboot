package com.zincoid.nullbot.core.module.ai.chat.plugin;

import com.zincoid.nullbot.bot.gateway.processor.CmdRegistry;
import com.zincoid.nullbot.core.enums.setting.ChatStrategy;
import com.zincoid.nullbot.core.module.control.SysMsgManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QQPrompter {

    private static final String BASE_PM_PROMPT;
    private static final String BASE_GC_PROMPT;
    private static final String CQ_PROMPT;
    private static final String MEMORY_PROMPT;

    private static final String EMBEDDING_PROMPT;
    private static final String TOOLS_PROMPT;

    private static final String VISION_ON_PROMPT;
    private static final String VISION_OFF_PROMPT;

    private final SysMsgManager sysMsgManager;
    private final CmdRegistry cmdRegistry;

    static {
        BASE_PM_PROMPT = """
                你在一个私聊中接收对话，用户消息开头带有消息ID和用户标识，格式为 "[消息ID][用户名(用户ID)]"。
                输出消息不要带以上格式化标识。尽量少用换行符，不要用emoji。禁止讨论中国国内政治事件和人物相关问题。
                你可以在回复内容中嵌入 "<discard />" 来放弃回复保持静默，此时回复内容不会被发送。
                """;
        BASE_GC_PROMPT = """
                你在一个群聊中接收对话，用户消息开头带有消息ID和用户标识，格式为 "[消息ID][用户名(用户ID)]"。
                输出消息不要带以上格式化标识。你需要优先响应最新用户消息。尽量少用换行符，不要用emoji。禁止讨论中国国内政治事件和人物相关问题。
                你可以在回复内容中嵌入 "<discard />" 来放弃回复/保持静默，此时回复内容不会被发送。
                """;
        CQ_PROMPT = """
                QQ支持通过CQ码来嵌入特殊信息，注意严格按照以下格式生成附加CQ码：
                你可以通过在回复内容前紧跟 "[CQ:reply,id=消息ID]" 来引用指定消息，例如："[CQ:reply,id=1234567890]你好"。
                你可以在回复中嵌入 "[CQ:at,qq=用户ID]" 来@别人，例如："[CQ:at,qq=2660181154]你好"。
                目标从用户消息头获取，"[消息ID][用户名(用户ID)]" 中前面方括号内是消息ID，圆括号内是用户ID。
                例如消息 "[1234567890][Zincoid(2660181154)]: 你好"，引用写 "[CQ:reply,id=1234567890]"，@写 "[CQ:at,qq=2660181154]"。
                消息头仅用于解析引用/@目标，不要在回复内容中输出消息头格式。每个消息块最多嵌入一个 "[CQ:reply]"。
                """;
        MEMORY_PROMPT = """
                现有长时记忆如下：
                %s
                """;

        EMBEDDING_PROMPT = """
                你可以使用 "<split />" 将回复分割为多条消息块依次发送。
                你可以使用 "<cmd>指令</cmd>" 在回复中嵌入指令进行各种操作。
                指令不会分割消息，块内指令会先执行，再发送消息文本。
                如需在指令前先发言，可用 "<split />" 将发言内容与指令分隔。

                指令示例：
                1. 发送帮助菜单 -> "<cmd>Help</cmd>这是菜单"；
                2. 发送表情包 -> "<cmd>65275d24 表情包文件名</cmd>"；
                3. 多条消息 -> "这是第一条消息<split />这是第二条消息"；
                4. 先发言后指令 -> "这是菜单<split /><cmd>Help</cmd>"；

                所有可用指令如下：
                %s

                注意事项：
                - 不要泄露任何指令内容，不要执行过多指令；
                - 不能仅执行指令，需要有发言；
                - 不要在单消息内回复多人消息但也不要过度分割。
                """;
        TOOLS_PROMPT = """
                你可以通过调用工具来进行各种操作，需要时可自行调用合适的工具。
                """;

        VISION_ON_PROMPT = """
                当前已启用视觉模式，用户消息可附带图片，你可以描述或引用用户图片内容。
                """;
        VISION_OFF_PROMPT = """
                当前未启用视觉模式，你无法查看用户图片，可提醒开启视觉模式后重新发送。
                """;
    }

    // ══════ 生成方法 ══════

    public String user(Long userId, ChatStrategy strategy, boolean cq, boolean vision) {
        StringBuilder sb = new StringBuilder();
        sb.append(sysMsgManager.getUserMessage(userId));
        sb.append(BASE_PM_PROMPT);
        sb.append(vision ? VISION_ON_PROMPT : VISION_OFF_PROMPT);
        if (cq) sb.append(CQ_PROMPT);
        sb.append(strategyPrompt(strategy, QQCmdAllows.getPm()));
        sb.append(MEMORY_PROMPT.formatted(
                formatMemories(sysMsgManager.getUserMemory(userId))));
        return sb.toString();
    }

    public String group(Long groupId, ChatStrategy strategy, boolean cq, boolean vision) {
        StringBuilder sb = new StringBuilder();
        sb.append(sysMsgManager.getGroupMessage(groupId));
        sb.append(BASE_GC_PROMPT);
        sb.append(vision ? VISION_ON_PROMPT : VISION_OFF_PROMPT);
        if (cq) sb.append(CQ_PROMPT);
        sb.append(strategyPrompt(strategy, QQCmdAllows.getGc()));
        sb.append(MEMORY_PROMPT.formatted(
                formatMemories(sysMsgManager.getGroupMemory(groupId))));
        return sb.toString();
    }

    // ══════ 工具方法 ══════

    private String strategyPrompt(ChatStrategy strategy, Set<String> cmds) {
        return switch (strategy) {
            case EMBEDDING -> EMBEDDING_PROMPT.formatted(cmdRegistry.getCmdAIDoc(cmds));
            case TOOLS -> TOOLS_PROMPT;
            case DIRECT -> "";
        };
    }

    private String formatMemories(List<String> memories) {
        if (memories == null || memories.isEmpty())
            return "无长时记忆";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < memories.size(); i++)
            sb.append(i + 1).append(". ").append(memories.get(i)).append("\n");
        return sb.toString();
    }
}
