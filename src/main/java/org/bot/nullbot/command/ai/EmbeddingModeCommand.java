package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"EmbeddingMode", "嵌入模式", "指令模式"})
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingModeCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String embedding = deepSeekClient.changeEmbedding();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[嵌入模式] \uD83D\uDD04已切换至: " + embedding, false);
            log.info("\t\t\t\t├─[AI.EmbeddingMode] 嵌入模式已切换 - {}", embedding);
        }else
            log.info("\t\t\t\t├─[AI.EmbeddingMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ EmbeddingMode 命令
                功能: 切换AI回复时的嵌入指令处理模式(非自定义提示词模式时生效)
                限权: %d
                格式: EmbeddingMode
                中文命令: 嵌入模式/指令模式""", getAccess()
        );
    }
}
