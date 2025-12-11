package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.GroupMsgDeleteNoticeHandler;
import com.mikuac.shiro.annotation.GroupPokeNoticeHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.ChatStorage;
import org.bot.nullbot.plugin.component.DeepSeekClient;
import org.bot.nullbot.plugin.util.DownloadUtil;
import org.bot.nullbot.plugin.util.MessageParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Shiro
@Component
@RequiredArgsConstructor
public class MonitorListener
{
    private static final Logger logger = LoggerFactory.getLogger(MonitorListener.class);
    private final CommandProcessor commandProcessor;
    private final ChatStorage chatStorage;
    private final DeepSeekClient deepSeekClient;
    private final FileStorageConfig fileStorageConfig;


    @FunctionControl(config = "enableImageCollect")
    @GroupMessageHandler
    @Async("virtualThreadExecutor")
    public void GroupImageCollect(Bot bot, GroupMessageEvent event) {
        List<Long> groupBypass = List.of(875310845L, 459358160L);
        if(groupBypass.contains(event.getGroupId())){
            boolean hasLogged = false;
            for(ArrayMsg msg : event.getArrayMsg()){
                if(msg.getType() == MsgTypeEnum.image){
                    if (!hasLogged){
                        logger.info("◉ [GroupMonitor:ImageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
                        hasLogged = true;
                    }
                    String originName =msg.getData().get("file");
                    String url = msg.getData().get("url");
                    String fileName = originName.substring(0, originName.lastIndexOf("."));
                    String info = DownloadUtil.downloadFile(url, fileStorageConfig.getImagePath() + "/monitor", fileName);
                    logger.info("└─[Saved] {}", info);
                }
            }
        }
    }

    @FunctionControl(config = "enableMessageCollect")
    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    @Async("virtualThreadExecutor")
    public void GroupMessageCollect(Bot bot, GroupMessageEvent event) {
        if(!event.getMessage().startsWith("/Chat")){
            logger.info("◉ [GroupMonitor:MessageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(event.getGroupId());
            chatMessages.add(new ChatMessage(event.getMessageId() ,"user", MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()), event.getSender().getUserId(), event.getSender().getNickname()));
            chatStorage.trimHistory(chatMessages, deepSeekClient.getDeepSeekConfig().getMaxMonitorLength());
            logger.info("└─[Record] {} item(s)", chatMessages.size());
        }
    }

    @FunctionControl(config = "enableKeywordDetect")
    @GroupMessageHandler
    @Async("virtualThreadExecutor")
    public void GroupKeywordDetect(Bot bot, GroupMessageEvent event) throws Exception {
        if (event.getMessage().contains("男娘")) {
            logger.info("◉ [GroupMonitor:Keyword] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("哪有男娘？"), event, false));
        }
        if (event.getMessage().contains("受着")) {
            logger.info("◉ [GroupMonitor:Keyword] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>("UserBan", List.of(event.getSender().getUserId().toString(), "1"), event, false));
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("你也受着"), event, false));
        }
    }

    @FunctionControl(config = "enablePokeDetect")
    @GroupPokeNoticeHandler
    @Async("virtualThreadExecutor")
    public void GroupPokeDetect(Bot bot, PokeNoticeEvent event) throws Exception {
        logger.info("◉ [GroupAction:Poke] 来自群 {} -> From {} to {}", event.getGroupId(), event.getUserId(), event.getTargetId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }

    @FunctionControl(config = "enableRecallDetect")
    @GroupMsgDeleteNoticeHandler
    @Async("virtualThreadExecutor")
    public void GroupRecallDetect(Bot bot, GroupMsgDeleteNoticeEvent event) throws Exception {
        logger.info("◉ [GroupMonitor:Recall] 来自群 {} -> {}", event.getGroupId(), event.getUserId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }
}