package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Shiro
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandListener
{
    private final CommandProcessor commandProcessor;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;


    @GroupMessageHandler
    @Async("ThreadExecutor")
    public void onGroupCommandInteraction(Bot bot, GroupMessageEvent event) throws Exception {
        if (event.getMessage().startsWith(commandPrefix)) {  // 检测普通命令
            log.info("◉ [GroupAction:Command] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }else if(event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply){  // 检测引用命令
            String slashCommand = event.getArrayMsg().get(1).getData().get("text");
            if(slashCommand != null && slashCommand.startsWith(commandPrefix)){
                log.info("◉ [GroupAction:ReplyCommand] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
                commandProcessor.processQQ(bot, new CommandEvent<>(event));
            }
        }
    }

    // @GroupMessageHandler
    // @Async("ThreadExecutor")
    // public void onGroupBasicCommandInteraction(Bot bot, GroupMessageEvent event) throws Exception {
    //     if (event.getMessage().startsWith(commandPrefix)) {  // 检测普通命令
    //         logger.info("◉ [GroupAction:Command] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
    //         commandProcessor.processQQ(bot, new CommandEvent<>(event));
    //     }
    // }

    // @GroupMessageHandler
    // @MessageHandlerFilter(reply = ReplyEnum.REPLY_ALL)
    // @Async("ThreadExecutor")
    // public void onGroupReplyCommandInteraction(Bot bot, GroupMessageEvent event) throws Exception {  // 检测引用命令
    //     String slashCommand = event.getArrayMsg().get(1).getData().get("text");
    //     if(slashCommand != null && slashCommand.startsWith(commandPrefix)){
    //         logger.info("◉ [GroupAction:ReplyCommand] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage());
    //         commandProcessor.processQQ(bot, new CommandEvent<>(event));
    //     }
    // }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("ThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception {
        log.info("◉ [GroupAction:At] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()));
        commandProcessor.processQQ(bot, new CommandEvent<>("Chat", event));
    }
}