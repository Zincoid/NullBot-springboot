package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.GroupMsgDeleteNoticeHandler;
import com.mikuac.shiro.annotation.GroupPokeNoticeHandler;
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
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
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
    private final CommandProcessor commandProcessor;
    private final ChatStorage chatStorage;
    private final DeepSeekClient deepSeekClient;
    private final FileStorageConfig fileStorageConfig;

    @FunctionControl(config = "imageCollect")
    @GroupMessageHandler
    @Async("ThreadExecutor")
    public void onGroupImageCollection(Bot bot, GroupMessageEvent event) {
        List<Long> groupBypass = List.of(875310845L, 459358160L);
        if(groupBypass.contains(event.getGroupId())){
            boolean hasLogged = false;
            for(ArrayMsg msg : event.getArrayMsg()){
                if(msg.getType() == MsgTypeEnum.image){
                    if (!hasLogged){
                        log.info("◉ [GroupMonitor:ImageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
                        hasLogged = true;
                    }
                    String originName =msg.getData().get("file");
                    String url = msg.getData().get("url");
                    String fileName = originName.substring(0, originName.lastIndexOf("."));
                    String info = DownloadUtil.downloadFile(url, fileStorageConfig.getImagePath() + "/monitor", fileName);
                    log.info("└─[Saved] {}", info);
                }
            }
        }
    }

    @FunctionControl(config = "messageCollect")
    // @GroupMessageHandler
    // @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    // @Async("ThreadExecutor")
    public void onGroupMessageCollection(Bot bot, GroupMessageEvent event) {  // AI Monitor 模式下并行调用存在严重问题 (已修改为串行调用)
        if(!event.getMessage().startsWith("/Chat")){
            log.info("◉ [GroupMonitor:MessageCollect] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()));
            List<ChatMessage> chatMessages = chatStorage.getMonitorHistory(event.getGroupId());
            chatMessages.add(new ChatMessage(event.getMessageId() ,"user", MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()), event.getSender().getUserId(), event.getSender().getNickname()));
            chatStorage.trimHistory(chatMessages, deepSeekClient.getDeepSeekConfig().getMaxMonitorLength());
            log.info("└─[Record] {} item(s)", chatMessages.size());
        }
    }

    @FunctionControl(config = "keywordDetect")
    @GroupMessageHandler
    @Async("ThreadExecutor")
    public void onGroupKeywordDetection(Bot bot, GroupMessageEvent event) throws Exception {  // 暂时直接设计针对群聊的规则
        if (event.getMessage().contains("男娘")) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"男娘\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            bot.sendGroupMsg(event.getGroupId(), "哪有男娘？", false);
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("哪有男娘？"), event, false));
        }
        if (event.getMessage().contains("受着") && event.getGroupId() == 459358160L) {
            log.info("◉ [GroupMonitor:Keyword] 检测到\"受着\"关键字 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>("UserBan", List.of(event.getSender().getUserId().toString(), "1"), event, false, false));
            // commandProcessor.processQQ(bot, new CommandEvent<>("Reply", List.of("你也受着"), event, false));
        }
    }

    @FunctionControl(config = "pokeDetect")
    @GroupPokeNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupPokeDetection(Bot bot, PokeNoticeEvent event) throws Exception {
        if(Objects.equals(event.getTargetId(), event.getSelfId())){
            log.info("◉ [GroupAction:Poke] 来自群 {} -> From {} to {} (已限制为戳Bot自己)", event.getGroupId(), event.getUserId(), event.getTargetId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @FunctionControl(config = "recallDetect")
    @GroupMsgDeleteNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupRecallDetection(Bot bot, GroupMsgDeleteNoticeEvent event) throws Exception {
        log.info("◉ [GroupMonitor:Recall] 来自群 {} -> {}", event.getGroupId(), event.getUserId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }
}