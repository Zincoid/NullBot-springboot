package com.zincoid.nullbot.bot.dispatcher.listener;

import com.mikuac.shiro.annotation.*;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.BotContext;
import com.zincoid.nullbot.core.annotation.FunctionControl;
import com.zincoid.nullbot.core.component.security.SecurityCodeScheduler;
import com.zincoid.nullbot.bot.dispatcher.CommandProcessor;
import com.zincoid.nullbot.bot.dispatcher.handler.impl.PermissionHandler;
import com.zincoid.nullbot.core.model.bot.CommandEvent;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;

@Slf4j
@Shiro
@Component
@RequiredArgsConstructor
public class CommandListener {

    /* 聊天机器人入口监听器 */
    /* Shiro 2.3.3 框架有BUG 回复消息中有@机器人和另一个人时会被判定为 AtEnum.NOT_NEED 的方法 */

    private final CommandProcessor commandProcessor;
    private final MonitorListener monitorListener;
    private final SecurityCodeScheduler securityCodeScheduler;
    private final PermissionHandler  permissionHandler;

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    @Value("${nullbot.admin-id}")
    private Long adminId;

    // ================================== 私聊动作捕获 ==================================

    @FunctionControl("PrivateCmd")
    @PrivateMessageHandler
    @Async("ThreadExecutor")
    public void onPrivateMessageInteraction(Bot bot, PrivateMessageEvent event) throws Exception {

        Long userId = event.getUserId();
        String userName = event.getPrivateSender().getNickname();
        String message = event.getMessage();

        // // 默认通知管理员
        // log.info("◉ [PrivateAction:Msg] 私聊 {}({}) -> {}", userName, userId, message);
        // bot.sendPrivateMsg(adminId, "\uD83D\uDCE9来自%s(%s)的消息:\n%s"
        //         .formatted(userName, userId, message), false);
        // bot.sendPrivateMsg(userId, "✉️已通知管理员", false);

        if (message.startsWith(commandPrefix)) {
            // 普通命令处理
            log.info("◉ [PrivateAction:Cmd] 私聊 {}({}) -> {}", userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        } else if (message.startsWith("#")) {
            // 授权命令处理
            log.info("◉ [PrivateAction:Auth] 私聊 {}({}) -> {}", userName, userId, message);
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
            String parsed = MsgParseUtil.formatUserMsg(bot, event.getArrayMsg());
            log.info("◉ [PrivateAction:Chat] 私聊 {}({}) -> {}", userName, userId, parsed);
            commandProcessor.processQQ(bot, new CommandEvent<>(
                    event, "Chat", List.of(parsed), false, false));
        }
    }

    @FunctionControl("PrivateCmd")
    @PrivatePokeNoticeHandler
    @Async("ThreadExecutor")
    public void onPrivatePokeInteraction(Bot bot, PokeNoticeEvent event) throws Exception {
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [PrivateAction:Poke] 私聊 -> From {} to {} (仅戳Bot)", event.getUserId(), event.getTargetId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    // ================================== 群聊动作捕获 ==================================

    @BotContext
    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    @Async("ThreadExecutor")
    public void onGroupMessageInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 指令输入捕获
        if (monitorListener.doGroupInputResponse(event)) {
            monitorListener.doGroupMsgCollect(bot, event);
            monitorListener.doGroupImgCollect(event);
            return;
        }
        // 串行调用 消息预处理 默认处理情况
        monitorListener.doGroupKeywordAct(bot, event);
        if (!monitorListener.doGroupAIAutoReply(bot, event))  // 按需 AI自动记录
            monitorListener.doGroupMsgCollect(bot, event);
        monitorListener.doGroupImgCollect(event);
        monitorListener.doGroupBottleAutoThrow(bot, event);

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String message = event.getMessage();

        if (message.startsWith(commandPrefix)) {
            // 普通命令处理
            log.info("◉ [GroupAction:Cmd] 群聊 {} - {}({}) -> {}", groupId, userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            // 引用命令处理
            JsonNode textNode = event.getArrayMsg().get(1).getData().get("text");
            if (textNode == null || !textNode.asString().startsWith(commandPrefix)) return;
            log.info("◉ [GroupAction:ReplyCmd] 群聊 {} - {}({}) -> {}", groupId, userName, userId, message);
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @BotContext
    @FunctionControl("PokeDetect")
    @GroupPokeNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupPokeInteraction(Bot bot, PokeNoticeEvent event) throws Exception {
        if (!BotCtxUtil.getSetting().isPokeDetect()) return;
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [GroupAction:Poke] 群聊 {} -> From {} to {} (仅戳Bot)", event.getGroupId(), event.getUserId(), event.getTargetId());
            commandProcessor.processQQ(bot, new CommandEvent<>(event));
        }
    }

    @BotContext
    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("ThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 默认处理情况
        // monitorListener.doGroupKeywordAct(bot, event);  // 禁用 关键词检测
        // if (!monitorListener.doGroupAIAutoReply(bot, event))  // 无需 AI即将回复
        //     monitorListener.doGroupMsgCollect(bot, event);  // 无需 AI自动记录
        monitorListener.doGroupImgCollect(event);
        monitorListener.doGroupBottleAutoThrow(bot, event);

        String parsed = MsgParseUtil.formatUserMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupAction:At] 群聊 {} - {}({}) -> {}",
                event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        commandProcessor.processQQ(bot, new CommandEvent<>(
                event, "Chat", List.of(parsed), true, true));
    }

    @BotContext
    @FunctionControl("RecallDetect")
    @GroupMsgDeleteNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupRecallInteraction(Bot bot, GroupMsgDeleteNoticeEvent event) throws Exception {
        if (!BotCtxUtil.getSetting().isRecallDetect()) return;
        log.info("◉ [GroupAction:Recall] 群聊 {} -> {}", event.getGroupId(), event.getUserId());
        commandProcessor.processQQ(bot, new CommandEvent<>(event));
    }
}