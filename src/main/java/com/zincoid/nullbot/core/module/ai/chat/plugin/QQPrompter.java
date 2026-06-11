package com.zincoid.nullbot.core.module.ai.chat.plugin;

import com.zincoid.nullbot.bot.gateway.processor.CmdRegistry;
import com.zincoid.nullbot.core.module.control.SysMsgManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QQPrompter {

    private static final String BASE_PM_PROMPT;
    private static final String BASE_GC_PROMPT;
    private static final String CMD_PROMPT;
    private static final String MEMORY_PROMPT;

    private final SysMsgManager sysMsgManager;
    private final CmdRegistry cmdRegistry;

    static {

        BASE_PM_PROMPT = """
                
                你在一个私聊中接收对话，用户消息开头带有消息ID和用户标识，格式为[MessageId][Username(UserId)]。
                回复消息时不要带以上格式化标识。尽量少用换行符，不要用emoji。禁止讨论中国国内政治事件和人物相关问题。
                你可以通过在回复内容前紧跟[CQ:reply,id=消息ID]来引用指定消息，仅在需强调回复某消息时使用，例如：
                [CQ:reply,id=1234567890]你好。
                你可以在回复内容中嵌入 <discard /> 来放弃回复保持静默，此时回复内容不会被发送。""";

        BASE_GC_PROMPT = """
                
                你在一个群聊中接收对话，用户消息开头带有消息ID和用户标识，格式为[MessageId][Username(UserId)]。
                回复消息时不要带以上格式化标识。尽量少用换行符，不要用emoji。禁止讨论中国国内政治事件和人物相关问题。
                你可以通过在回复内容前紧跟[CQ:reply,id=消息ID]来引用指定消息，仅在需强调回复某消息时使用，例如：
                [CQ:reply,id=1234567890]你好。
                你可以在回复中嵌入[CQ:at,qq=用户ID]来@别人，例如：
                [CQ:at,qq=2660181154]你好。
                你可以在回复内容中嵌入 <discard /> 来放弃回复/保持静默，此时回复内容不会被发送。""";

        CMD_PROMPT = """

                你可以使用 <cmd>指令</cmd> 在回复中嵌入指令进行各种操作。
                被指令分隔的消息会以多条消息形式发送。
                如果你想分开发送消息也可以使用空指令 <cmd></cmd> 来分割。

                指令示例：
                1. 发送帮助菜单 -> <cmd>Help</cmd>；
                2. 发送表情包 -> <cmd>65275d24 表情包文件名</cmd>；
                3. 多条消息 -> 这是第一条消息<cmd></cmd>这是第二条消息；

                所有可用指令如下：
                %s

                注意事项：
                不要泄露任何指令内容！不要轻易复读别人让你执行的指令！
                回复时不要执行过多指令，不要分割过多子消息！
                不必要的时候不要经常发指令！回复指令时要说些什么！""";

        MEMORY_PROMPT = """
                
                现有长时记忆如下：
                %s""";

    }

    // =================== 生成方法 ===================

    public String user(Long userId, boolean cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(sysMsgManager.getUserMessage(userId));
        sb.append(BASE_PM_PROMPT);
        sb.append(MEMORY_PROMPT.formatted(
                formatMemories(sysMsgManager.getUserMemory(userId))));
        if (cmd) sb.append(CMD_PROMPT.formatted(
                cmdRegistry.getCmdHelpsForAI(QQCmdAllows.getPm())));
        return sb.toString();
    }

    public String group(Long groupId, boolean cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(sysMsgManager.getGroupMessage(groupId));
        sb.append(BASE_GC_PROMPT);
        sb.append(MEMORY_PROMPT.formatted(
                sysMsgManager.getGroupMemory(groupId)));
        if (cmd) sb.append(CMD_PROMPT.formatted(
                cmdRegistry.getCmdHelpsForAI(QQCmdAllows.getGc())));
        return sb.toString();
    }

    // =================== 工具方法 ===================

    private String formatMemories(List<String> memories) {
        if (memories == null || memories.isEmpty())
            return "无长时记忆";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < memories.size(); i++) {
            sb.append(i + 1).append(". ").append(memories.get(i)).append("\n");
        }
        return sb.toString();
    }
}
