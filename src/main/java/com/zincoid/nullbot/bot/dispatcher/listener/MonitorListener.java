package com.zincoid.nullbot.bot.dispatcher.listener;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
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
import com.zincoid.nullbot.core.util.BotCtxUtil;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorListener {

    /* 聊天机器人工具监听器 */

    private final BotInputManager botInputManager;
    private final CommandProcessor commandProcessor;
    private final MsgWindowChatMemory msgWindowChatMemory;
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
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "DriftBottle", List.of("-auto"), false, false));

    }

    @FunctionControl("AIAutoReply")
    public boolean doGroupAIAutoReply(Bot bot, GroupMessageEvent event) throws Exception {
        if (!BotCtxUtil.getSetting().isAutoReply()) return false;
        if (event.getMessage().startsWith(commandPrefix)) {
            return false;
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            JsonNode textNode = event.getArrayMsg().get(1).getData().get("text");
            if (textNode != null && textNode.asString().startsWith(commandPrefix)) return false;
        }

        double freq = BotCtxUtil.getSetting().getReplyFrequency();
        if (freq < Math.random()) return false;
        String parsed = MsgParseUtil.formatUserMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:AIAutoReply] 自动回复至群聊 {}", event.getGroupId());
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "Chat", List.of(parsed), false, false));
        return true;
    }

    // =================== 资源监听方法 ===================

    @FunctionControl("ImgCollect")
    public void doGroupImgCollect(GroupMessageEvent event) {  // 缺失群目录时数据库无法插入文件条目需先SYNC
        if (!BotCtxUtil.getSetting().isImageCollect()) return;

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.image) continue;
            log.info("◉ [GroupMonitor:ImgCollect] 来自群 {} - {}({}) -> Image", groupId, userName, userId);
            String originName = msg.getData().get("file").asString();
            String url = msg.getData().get("url").asString();
            String fileName = originName.substring(0, originName.lastIndexOf("."));
            String filePath = storageProperties.getImagePath() + "/monitor/" + groupId;
            try {
                FileInfo fileInfo = fileService.upload(url, filePath, fileName, userId);
                log.info("└─[Saved] {}", fileInfo.getName());
            } catch (Exception e) {
                log.info("└─[Error] {}", e.getMessage());
                throw e;
            }
        }
    }

    @FunctionControl("MsgCollect")
    public void doGroupMsgCollect(Bot bot, GroupMessageEvent event) {
        if (!BotCtxUtil.getSetting().isMessageCollect()) return;

        if (event.getMessage().startsWith(commandPrefix + "Chat") || event.getMessage().startsWith(commandPrefix + "对话")) return;  // 按需 AI自动记录
        String parsed = MsgParseUtil.formatUserMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:MsgCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        msgWindowChatMemory.add(
                ChatScope.MONITOR + "_" + event.getGroupId(),
                QQMessage.user(parsed).with(event.getGroupId(), event.getUserId(), event.getSender().getNickname()).id(event.getMessageId())
        );
    }

    @FunctionControl("KeywordAct")
    public void doGroupKeywordAct(Bot bot, GroupMessageEvent event) throws Exception {
        if (!BotCtxUtil.getSetting().isKeywordDetect()) return;

        if (event.getMessage().contains("男娘")) {
            log.info("◉ [GroupMonitor:KeywordAct] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            bot.sendGroupMsg(event.getGroupId(), "哪有男娘？", false);
            // commandProcessor.processQQ(bot, new CommandEvent<>(event, "Reply", List.of("哪有男娘？"), false, false));
        }
        if (event.getMessage().contains("受着")) {
            log.info("◉ [GroupMonitor:KeywordAct] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>(event, "UserBan", List.of(event.getUserId().toString(), "1"), false, false));
            // commandProcessor.processQQ(bot, new CommandEvent<>(event, "Reply", List.of("你也受着"), false, false));
        }
    }
}