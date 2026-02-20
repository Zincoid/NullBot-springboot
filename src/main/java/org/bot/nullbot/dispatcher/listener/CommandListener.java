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
    private final MonitorListener monitorListener;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;


    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    @Async("ThreadExecutor")
    public void onGroupMessageInteraction(Bot bot, GroupMessageEvent event) throws Exception
    {
        // 串行调用 消息预处理 指令输入捕获
        if (monitorListener.onGroupNextInputDetection(event)) {
            monitorListener.onGroupMessageCollection(bot, event);
            monitorListener.onGroupImageCollection(event);
            return;
        }
        // 串行调用 消息预处理 默认处理情况
        monitorListener.onGroupKeywordDetection(bot, event);
        if (!monitorListener.onGroupAIAutoReply(bot, event))  // 触发自动发言会记录当前消息 忽略消息收集
            monitorListener.onGroupMessageCollection(bot, event);
        monitorListener.onGroupImageCollection(event);

        if (event.getMessage().startsWith(commandPrefix)) {  // 检测普通命令
            log.info("◉ [GroupAction:Command] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage().replaceAll("\\R", " "));
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        } else if (event.getArrayMsg().size() >= 2 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply){  // 检测引用命令
            String slashCommand = event.getArrayMsg().get(1).getData().get("text");
            if(slashCommand != null && slashCommand.startsWith(commandPrefix)){
                log.info("◉ [GroupAction:ReplyCommand] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getMessage().replaceAll("\\R", " "));
                commandProcessor.processQQ(bot, new CommandEvent<>(event));
            }
        }
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("ThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception
    {
        // 串行调用 消息预处理 默认处理情况
        // monitorListener.onGroupKeywordDetection(bot, event);  // 禁用 关键词检测
        // if (!monitorListener.onGroupAIAutoReply(bot, event))  // 无需调用 AI即将回复
        //     monitorListener.onGroupMessageCollection(bot, event);  // 无需调用 AI自动记录
        monitorListener.onGroupImageCollection(event);

        log.info("◉ [GroupAction:At] 来自群 {} - {}({}) -> {}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg()));
        commandProcessor.processQQ(bot, new CommandEvent<>(event, "Chat", true, true));
    }

    // 框架有BUG 回复消息中有@机器人和另一个人时会被判定为 AtEnum.NOT_NEED 的方法 暂时不知道怎么修
}