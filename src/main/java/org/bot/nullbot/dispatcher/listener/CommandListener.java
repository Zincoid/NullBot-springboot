package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.GroupPokeNoticeHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.MessageParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Shiro
@Component
@RequiredArgsConstructor
public class CommandListener
{
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final CommandProcessor commandProcessor;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;


    @GroupMessageHandler
    @Async("virtualThreadExecutor")
    public void onGroupCommandInteraction(Bot bot, GroupMessageEvent event) throws Exception {
        if (event.getMessage().startsWith(commandPrefix)) {  // 检测普通命令
            logger.info("◉ [GroupAction:Command] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }else if(event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply){  // 检测引用命令
            String slashCommand = event.getArrayMsg().get(1).getData().get("text");
            if(slashCommand != null && slashCommand.startsWith(commandPrefix)){
                logger.info("◉ [GroupAction:ReplyCommand] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
                commandProcessor.processQQ(bot, new CommandEvent<>(event));
            }
        }
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("virtualThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception {
        logger.info("◉ [GroupAction:At] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()));
        commandProcessor.processQQ(bot, new CommandEvent<>("Chat", event));
    }

    @GroupPokeNoticeHandler
    @Async("virtualThreadExecutor")
    public void onGroupPokeInteraction(Bot bot, PokeNoticeEvent event) throws Exception {
        logger.info("◉ [GroupAction:Poke] 来自群 {} -> From {} to {}", event.getGroupId(), event.getUserId(), event.getTargetId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }
}