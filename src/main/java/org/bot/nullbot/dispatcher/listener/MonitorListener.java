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
import org.bot.nullbot.component.control.BotNextInputer;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Shiro
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorListener
{
    private final BotNextInputer botNextInputer;
    private final CommandProcessor commandProcessor;
    private final ChatStorage chatStorage;
    private final DeepSeekProperties deepSeekProperties;
    private final FileStorageProperties fileStorageProperties;
    private final SettingService settingService;
    private final FileService fileService;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;

    // =================== 输入监听方法 ===================

    public boolean onGroupNextInputDetection(GroupMessageEvent event) {
        return botNextInputer.response(event.getUserId(), event.getMessage());
    }

    // =================== 串行监听方法 ===================

    @FunctionControl(config = "AIAutoReply")
    public boolean onGroupAIAutoReply(Bot bot, GroupMessageEvent event) throws Exception
    {
        if (!settingService.isAutoReply(event.getGroupId())) return false;
        if (event.getMessage().startsWith(commandPrefix)) {
            return false;
        } else if (event.getArrayMsg().size() >= 2 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            String slashCommand = event.getArrayMsg().get(1).getData().get("text");
            if(slashCommand != null && slashCommand.startsWith(commandPrefix)) return false;
        }

        double freq = settingService.getReplyFrequency(event.getGroupId());
        if (freq > Math.random()) {
            log.info("◉ [GroupMonitor:AIAutoReply] 自动回复至 群聊 {}", event.getGroupId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event, "Chat", false, false));
            return true;
        } else
            return false;
    }

    @FunctionControl(config = "ImgCollect")
    public void onGroupImageCollection(GroupMessageEvent event)  // 群目录不存在时数据库无法插入详情文件条目 需手动SYNC
    {
        if(!settingService.isImageCollect(event.getGroupId())) return;

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        boolean hasLogged = false;
        for(ArrayMsg msg : event.getArrayMsg()){
            if(msg.getType() == MsgTypeEnum.image){
                if (!hasLogged) {
                    // log.info("◉ [GroupMonitor:ImageCollect] 来自群 {} - {}({}) -> {}", groupId, userName, userId, event.getMessage());
                    log.info("◉ [GroupMonitor:ImageCollect] 来自群 {} - {}({}) -> Image", groupId, userName, userId);
                    hasLogged = true;
                }
                String originName =msg.getData().get("file");
                String url = msg.getData().get("url");
                String fileName = originName.substring(0, originName.lastIndexOf("."));
                String filePath = fileStorageProperties.getImagePath() + "/monitor/" + groupId;
                try {
                    FileInfo fileInfo = DownloadUtil.downloadFile(url, filePath, fileName, "├─ ");
                    if(!fileService.addFileRecordForBot(
                            filePath,
                            fileInfo.getFileName(),
                            fileInfo.getFileSize(),
                            fileInfo.getLastModified(),
                            userId, userName)
                    ) {
                        log.info("├─[Error] DbSave Failed");
                    }
                    log.info("└─[Saved] {}", fileInfo.getFileName());
                } catch (Exception e) {
                    log.info("└─[Error] {}", e.getMessage());
                    throw e;
                }
            }
        }
    }

    @FunctionControl(config = "MsgCollect")
    public void onGroupMessageCollection(Bot bot, GroupMessageEvent event)
    {
        if (!settingService.isMessageCollect(event.getGroupId())) return;
        if (!(event.getMessage().startsWith(commandPrefix + "Chat") || event.getMessage().startsWith(commandPrefix + "对话"))) {  // Chat 命令会自动记录消息 跳过
            log.info("◉ [GroupMonitor:MessageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()));
            List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(event.getGroupId());
            chatMessages.add(new ChatMessage(event.getMessageId() ,"user", MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()), event.getUserId(), event.getSender().getNickname()));
            chatStorage.trimHistory(chatMessages, deepSeekProperties.getMaxMonitorLength());
            log.info("└─[Recorded] {} Message(s)", chatMessages.size());
        }
    }

    @FunctionControl(config = "KeyDetect")
    public void onGroupKeywordDetection(Bot bot, GroupMessageEvent event) throws Exception
    {
        if (!settingService.isKeywordDetect(event.getGroupId())) return;
        if (event.getMessage().contains("男娘")) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            bot.sendGroupMsg(event.getGroupId(), "哪有男娘？", false);
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("哪有男娘？"), event, false, false));
        }
        if (event.getMessage().contains("受着")) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>("UserBan", List.of(event.getUserId().toString(), "1"), event, false, false));
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("你也受着"), event, false, false));
        }
    }

    // =================== 独占监听方法 ===================

    @FunctionControl(config = "PokeDetect")
    @GroupPokeNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupPokeDetection(Bot bot, PokeNoticeEvent event) throws Exception
    {
        if (!settingService.isPokeDetect(event.getGroupId())) return;
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [GroupAction:Poke] 来自群 {} -> From {} to {} (已限制为戳Bot自己)", event.getGroupId(), event.getUserId(), event.getTargetId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @FunctionControl(config = "PrivateCmd")
    @PrivatePokeNoticeHandler
    @Async("ThreadExecutor")
    public void onPrivatePokeDetection(Bot bot, PokeNoticeEvent event) throws Exception
    {
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [PrivateAction:Poke] 来自私信 -> From {} to {} (已限制为戳Bot自己)", event.getUserId(), event.getTargetId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @FunctionControl(config = "RecallDetect")
    @GroupMsgDeleteNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupRecallDetection(Bot bot, GroupMsgDeleteNoticeEvent event) throws Exception
    {
        if (!settingService.isRecallDetect(event.getGroupId())) return;
        log.info("◉ [GroupMonitor:Recall] 来自群 {} -> {}", event.getGroupId(), event.getUserId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }
}