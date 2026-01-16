package org.bot.nullbot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.TtsClient;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Tts", "语音合成"})
@Component
@Slf4j
@RequiredArgsConstructor
public class TtsCommand implements Command
{
    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[语音合成] ❌无参数");
            String message = String.join(" ", params.subList(0, params.size()));
            String base64;
            try {
                base64 = ttsClient.synthesize(message);
            } catch (Exception e) {
                throw new NullBotMsgException("[语音合成] ❌" + e.getMessage());
            }
            String response = MsgUtils.builder()
                    .voice("base64://" + base64)
                    .build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Tts] 已回复 - {}", message.replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[语音合成] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Tts 命令
                功能: 文字转语音
                限权: %d 级
                格式: Tts [文本]
                中文命令: 语音合成""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Tts 命令
                功能: 文字转语音并发送到群中
                限权: %d 级
                格式: Tts [文本]
                注意: 当你想要发送语音代替文字回复时使用该命令！""", getAccess()
        );
    }
}
