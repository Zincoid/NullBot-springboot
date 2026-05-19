package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMsgDeleteNoticeHandler;
import com.mikuac.shiro.annotation.GroupPokeNoticeHandler;
import com.mikuac.shiro.annotation.PrivatePokeNoticeHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.component.control.BotInputManager;
import org.bot.nullbot.config.prop.DeepSeekProperties;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.service.SettingService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Slf4j
@Shiro
@Component
@RequiredArgsConstructor
public class MonitorListener {

    private final BotInputManager botInputManager;
    private final CommandProcessor commandProcessor;
    private final ChatStorage chatStorage;
    private final DeepSeekProperties deepSeekProperties;
    private final FileStorageProperties fileStorageProperties;
    private final SettingService settingService;
    private final FileService fileService;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;

    // =================== 输入响应方法 ===================

    public boolean onGroupNextInputDetection(GroupMessageEvent event) {
        return botInputManager.response(event.getGroupId(), event.getUserId(), event.getMessage());
    }

    // =================== 自动动作方法 ===================

    @FunctionControl(value = "BottleAutoThrow", enabled = false)
    public void onGroupBottleAutoThrow(Bot bot, GroupMessageEvent event) throws Exception {
        double freq = 0.001;  // 固定自动投出频率
        if (freq < Math.random()) return;
        log.info("◉ [GroupMonitor:BottleAutoThrow] 用户 {} 自动投出漂流瓶", event.getUserId());
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "DriftBottle", List.of("-auto"), false, false));

    }

    @FunctionControl("AIAutoReply")
    public boolean onGroupAIAutoReply(Bot bot, GroupMessageEvent event) throws Exception {
        if (!settingService.isAutoReply(event.getGroupId())) return false;

        if (event.getMessage().startsWith(commandPrefix)) {
            return false;
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            JsonNode textNode = event.getArrayMsg().get(1).getData().get("text");
            if (textNode != null && textNode.asString().startsWith(commandPrefix)) return false;
        }
        double freq = settingService.getReplyFrequency(event.getGroupId());
        if (freq < Math.random()) return false;
        String parsed = MessageParseUtil.parseArrayMsgToSimple(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:AIAutoReply] 自动回复至 群聊 {}", event.getGroupId());
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "Chat", List.of(parsed), false, false));
        return true;
    }

    // =================== 资源监听方法 ===================

    @FunctionControl("ImgCollect")
    public void onGroupImageCollection(GroupMessageEvent event) {  // 缺失群目录时数据库无法插入文件条目需先SYNC
        if (!settingService.isImageCollect(event.getGroupId())) return;

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.image) continue;
            log.info("◉ [GroupMonitor:ImageCollect] 来自群 {} - {}({}) -> Image", groupId, userName, userId);
            String originName = msg.getData().get("file").asString();
            String url = msg.getData().get("url").asString();
            String fileName = originName.substring(0, originName.lastIndexOf("."));
            String filePath = fileStorageProperties.getImagePath() + "/monitor/" + groupId;
            try {
                FileInfo fileInfo = DownloadUtil.downloadFile(
                        url, filePath, fileName, "├─ ");
                if (!fileService.addRecordOnly(
                        filePath,
                        fileInfo.getFileName(),
                        fileInfo.getFileSize(),
                        fileInfo.getLastModified(),
                        userId, userName
                )) {
                    log.info("├─[Error] DbSave Failed");
                }
                log.info("└─[Saved] {}", fileInfo.getFileName());
            } catch (Exception e) {
                log.info("└─[Error] {}", e.getMessage());
                throw e;
            }
        }
    }

    @FunctionControl("MsgCollect")
    public void onGroupMessageCollection(Bot bot, GroupMessageEvent event) {
        if (!settingService.isMessageCollect(event.getGroupId())) return;
        if (event.getMessage().startsWith(commandPrefix + "Chat") || event.getMessage().startsWith(commandPrefix + "对话")) return;  // Chat 命令会自动记录消息 跳过
        String parsed = MessageParseUtil.parseArrayMsgToSimple(bot, event.getArrayMsg());
        log.info("◉ [GroupMonitor:MessageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(event.getGroupId());
        chatMessages.add(new ChatMessage(event.getMessageId() , event.getUserId(), event.getSender().getNickname(), "user", parsed));
        chatStorage.trimHistory(chatMessages, deepSeekProperties.getMaxMonitorLength());
        // log.info("└─[Recorded] {} Message(s)", chatMessages.size());
    }

    @FunctionControl("KeyDetect")
    public void onGroupKeywordDetection(Bot bot, GroupMessageEvent event) throws Exception {
        if (!settingService.isKeywordDetect(event.getGroupId())) return;
        if (event.getMessage().contains("男娘")) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            bot.sendGroupMsg(event.getGroupId(), "哪有男娘？", false);
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("哪有男娘？"), event, false, false));
        }
        if (event.getMessage().contains("受着")) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>(event, "UserBan", List.of(event.getUserId().toString(), "1"), false, false));
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("你也受着"), event, false, false));
        }
    }
}