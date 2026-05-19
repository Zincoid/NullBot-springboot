package org.bot.nullbot.dispatcher.listener;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.component.security.SecurityCodeScheduler;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.dispatcher.handler.impl.PermissionHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Slf4j
@Shiro
@Component
@RequiredArgsConstructor
public class CommandListener {

    /* Shiro 2.3.3 框架有BUG 回复消息中有@机器人和另一个人时会被判定为 AtEnum.NOT_NEED 的方法 */

    private final CommandProcessor commandProcessor;
    private final MonitorListener monitorListener;
    private final SecurityCodeScheduler securityCodeScheduler;
    private final PermissionHandler  permissionHandler;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    @Value("${nullbot.admin-id}")
    private Long adminId;

    @FunctionControl(id = "PrivateCmd")
    @PrivateMessageHandler
    @Async("ThreadExecutor")
    public void onPrivateMessageInteraction(Bot bot, PrivateMessageEvent event) throws Exception {

        Long userId = event.getUserId();
        String userName = event.getPrivateSender().getNickname();
        String message = event.getMessage();

        if (message.startsWith(commandPrefix)) {
            // 普通命令处理
            log.info("◉ [PrivateAction:Command] 来自 {}({}) -> {}", userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        } else if (message.startsWith("#")) {
            // 授权命令处理
            log.info("◉ [PrivateAction:Authorize] 来自 {}({}) -> {}", userName, userId, message);
            if (securityCodeScheduler.validateCode("access", message.substring(1))) {
                permissionHandler.addAllowedPrivateUser(userId);
                log.info("└─[Success] {}({}) 已授权", userName, userId);
                bot.sendPrivateMsg(userId, "✅已授权", false);
                return;
            }
            log.info("└─[Fail] {}({}) 访问码错误", userName, userId);
            bot.sendPrivateMsg(userId, "❌访问码错误", false);
        } else {
            // 私聊对话处理
            String parsed = MessageParseUtil.parseArrayMsgToSimple(bot, event.getArrayMsg());
            log.info("◉ [PrivateAction:AIChat] 来自 {}({}) -> {}", userName, userId, parsed);
            commandProcessor.processQQ(bot, new CommandEvent<>(
                    event, "Chat", List.of(parsed), false, false));
        }

        // // 默认通知管理员
        // log.info("◉ [PrivateAction:Notice] 来自 {}({}) -> {}", userName, userId, message);
        // bot.sendPrivateMsg(adminId, "\uD83D\uDCE9来自%s(%s)的私信:\n%s"
        //         .formatted(userName, userId, message), false);
        // bot.sendPrivateMsg(userId, "✉️已通知管理员", false);
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    @Async("ThreadExecutor")
    public void onGroupMessageInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 指令输入捕获
        if (monitorListener.onGroupNextInputDetection(event)) {
            monitorListener.onGroupMessageCollection(bot, event);
            monitorListener.onGroupImageCollection(event);
            return;
        }
        // 串行调用 消息预处理 默认处理情况
        monitorListener.onGroupKeywordDetection(bot, event);
        if (!monitorListener.onGroupAIAutoReply(bot, event))  // 按需 AI自动记录
            monitorListener.onGroupMessageCollection(bot, event);
        monitorListener.onGroupImageCollection(event);
        monitorListener.onGroupBottleAutoThrow(bot, event);

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String message = event.getMessage();

        if (message.startsWith(commandPrefix)) {
            // 普通命令处理
            log.info("◉ [GroupAction:Command] 来自群 {} - {}({}) -> {}", groupId, userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            // 引用命令处理
            JsonNode textNode = event.getArrayMsg().get(1).getData().get("text");
            if (textNode == null || !textNode.asString().startsWith(commandPrefix)) return;
            log.info("◉ [GroupAction:ReplyCommand] 来自群 {} - {}({}) -> {}", groupId, userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("ThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 默认处理情况
        // monitorListener.onGroupKeywordDetection(bot, event);  // 禁用 关键词检测
        // if (!monitorListener.onGroupAIAutoReply(bot, event))  // 无需 AI即将回复
        //     monitorListener.onGroupMessageCollection(bot, event);  // 无需 AI自动记录
        monitorListener.onGroupImageCollection(event);
        monitorListener.onGroupBottleAutoThrow(bot, event);

        String parsed = MessageParseUtil.parseArrayMsgToSimple(bot, event.getArrayMsg());
        log.info("◉ [GroupAction:At] 来自群 {} - {}({}) -> {}",
                event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "Chat", List.of(parsed), true, true));
    }
}