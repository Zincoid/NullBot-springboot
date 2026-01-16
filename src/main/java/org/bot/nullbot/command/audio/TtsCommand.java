package org.bot.nullbot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.TtsClient;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.TtsTemplateService;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandMapping({"Tts", "语音合成"})
@Component
@Slf4j
@RequiredArgsConstructor
public class TtsCommand implements Command
{
    private final TtsTemplateService ttsTemplateService;
    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.size() < 2) throw new NullBotMsgException("[语音合成] ❌参数不足");

            String option = params.getFirst();
            if ("-clone".equals(option)) {
                switch (params.get(1)) {
                    case "save" -> {
                        ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
                        if (reply.getType() != MsgTypeEnum.reply)
                            throw new NullBotMsgException("[语音合成] ❌需引用模板音频");
                        if (params.size() < 4)
                            throw new NullBotMsgException("[语音合成] ❌新模板参数不足");
                        GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                        Map<String, String> recordMap = MessageParseUtil.parseGroupRawMessageAsRecordMap(replyMsg.getRawMessage());
                        Map<String, String> fileMap = MessageParseUtil.parseGroupRawMessageAsFileMap(replyMsg.getRawMessage());
                        List<String> urls = new ArrayList<>();
                        urls.addAll(recordMap.values());
                        urls.addAll(fileMap.values());
                        for (String url : urls) {

                        }
                        // ttsTemplateService.addTemplate();
                    }
                }
            }
            if ("-synth".equals(option)) {
                String message = String.join(" ", params.subList(1, params.size()));
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
                log.info("\t\t\t\t├─[Tts] 已回复合成语音: {}", message.replaceAll("\\R", " "));
            }
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
