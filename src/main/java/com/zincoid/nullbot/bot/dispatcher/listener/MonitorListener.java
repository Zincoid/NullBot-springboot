package com.zincoid.nullbot.bot.dispatcher.listener;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.FunctionControl;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.bot.dispatcher.CommandProcessor;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.context.BotCtx;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorListener {

    /* 聊天机器人工具监听器 */

    private final BotInputManager botInputManager;
    private final CommandProcessor commandProcessor;
    private final MsgWindowMemory msgWindowMemory;
    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;

    // =================== 输入响应方法 ===================

    public boolean doGroupInputResponse(GroupMessageEvent event) {
        return botInputManager.response(event.getGroupId(), event.getUserId(), event.getMessage());
    }

    // =================== 自动动作方法 ===================

    @FunctionControl(value = "BottleAutoThrow", enabled = false)
    public void doGroupBottleAutoThrow(Bot bot, GroupMessageEvent event) throws Exception {
        double freq = 0.001;  // 固定自动投出频率
        if (freq < Math.random()) return;
        log.info("◉ [GroupMonitor:BottleAutoThrow] 自动投出漂流瓶 {} -> {}", event.getUserId(), event.getMessage());
        commandProcessor.processQQ(bot, CommandEvent.of(event, "Bottle", List.of("-auto"), false, false));
    }

    @FunctionControl("AIAutoReply")
    public boolean doGroupAIAutoReply(Bot bot, GroupMessageEvent event) throws Exception {
        if (!BotCtx.getSetting().isAutoReply()) return false;
        if (event.getMessage().startsWith(commandPrefix)) {
            return false;
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            if (event.getArrayMsg().get(1).getStringData("text").startsWith(commandPrefix)) return false;
        }

        double freq = BotCtx.getSetting().getReplyFrequency();
        if (freq < Math.random()) return false;
        String parsed = MsgParseUtil.formatMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:AIAutoReply] 自动回复至群聊 {}", event.getGroupId());
        commandProcessor.processQQ(bot, CommandEvent.of(event, "Chat", List.of(parsed), false, false));
        return true;
    }

    // =================== 资源监听方法 ===================

    @FunctionControl("ImgCollect")
    public void doGroupImgCollect(GroupMessageEvent event) {  // 缺失群目录时数据库无法插入文件条目需先SYNC
        if (!BotCtx.getSetting().isImageCollect()) return;

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String filePath = storageProperties.getImagePath() + "/monitor/" + groupId;

        Map<String, String> imageMap = MsgParseUtil.extractImgMap(event.getArrayMsg());
        for (Map.Entry<String, String> entry : imageMap.entrySet()) {
            log.info("◉ [GroupMonitor:ImgCollect] 来自群 {} - {}({}) -> Image", groupId, userName, userId);
            String filename = entry.getKey();
            String url = entry.getValue();
            try {
                FileInfo fileInfo = fileService.upload(url, filePath, filename.substring(0, filename.lastIndexOf(".")), userId);
                log.info("└─[Saved] {}", fileInfo.getName());
            } catch (Exception e) {
                log.info("└─[Error] {}", e.getMessage());
                throw e;
            }
        }
    }

    @FunctionControl("MsgCollect")
    public void doGroupMsgCollect(Bot bot, GroupMessageEvent event) {
        if (!BotCtx.getSetting().isMessageCollect()) return;

        if (event.getMessage().startsWith(commandPrefix + "Chat") || event.getMessage().startsWith(commandPrefix + "对话")) return;  // 按需 AI自动记录
        String parsed = MsgParseUtil.formatMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:MsgCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        msgWindowMemory.add(
                ChatScope.MONITOR + "_" + event.getGroupId(),
                QQMessage.user(parsed).with(event.getGroupId(), event.getUserId(), event.getSender().getNickname()).id(event.getMessageId())
        );
    }

    @FunctionControl("KeywordAct")
    public void doGroupKeywordAct(Bot bot, GroupMessageEvent event) throws Exception {
        if (!BotCtx.getSetting().isKeywordDetect()) return;

        if (event.getMessage().contains("男娘")) {
            log.info("◉ [GroupMonitor:KeywordAct] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            bot.sendGroupMsg(event.getGroupId(), "哪有男娘？", false);
            // commandProcessor.processQQ(bot, new CommandEvent<>(event, "Reply", List.of("哪有男娘？"), false, false));
        }
        if (event.getMessage().contains("受着")) {
            log.info("◉ [GroupMonitor:KeywordAct] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, CommandEvent.of(event, "UserBan", List.of(event.getUserId().toString(), "1"), false, false));
            // commandProcessor.processQQ(bot, CommandEvent.of(event, "Reply", List.of("你也受着"), false, false));
        }
    }
}