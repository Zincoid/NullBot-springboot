package com.zincoid.nullbot.bot.gateway.listener;

import com.mikuac.shiro.annotation.*;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.zincoid.nullbot.bot.gateway.handler.AuthHandler;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.bot.gateway.processor.CmdProcessor;
import com.zincoid.nullbot.core.properties.bot.CmdProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.BotContext;
import com.zincoid.nullbot.core.annotation.FuncControl;
import com.zincoid.nullbot.core.module.security.SecurityCodeScheduler;
import com.zincoid.nullbot.core.context.BotCtx;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Shiro
@Component
@RequiredArgsConstructor
@BotContext
public class BotListener {

    /* 聊天机器人入口监听器 */
    /* Shiro 2.3.3 框架有BUG 回复消息中有@机器人和另一个人时会被判定为 AtEnum.NOT_NEED 的方法 */

    private final BotMonitor botMonitor;
    private final CmdProcessor cmdProcessor;
    private final SecurityCodeScheduler securityCodeScheduler;
    private final AuthHandler authHandler;
    private final CmdProperties cmdProperties;

    @Value("${bot.admin-id}")
    private Long adminId;

    // ================================== 私聊动作捕获 ==================================

    @FuncControl("PrivateCmd")
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

        if (message.startsWith(cmdProperties.getPrefix())) {
            // 普通命令处理
            log.info("◉ [PrivateAction:Cmd] 私聊 {}({}) -> {}", userName, userId, message);
            cmdProcessor.processQQ(bot, CmdEvent.of(event));
        } else if (message.startsWith("#")) {
            // 授权命令处理
            log.info("◉ [PrivateAction:Auth] 私聊 {}({}) -> {}", userName, userId, message);
            if (securityCodeScheduler.validateCode("access", message.substring(1))) {
                authHandler.addAllowedPrivateUser(userId);
                log.info("└─[Success] {}({}) 已授权", userName, userId);
                bot.sendPrivateMsg(userId, "✅已授权", false);
                return;
            }
            log.info("└─[Fail] {}({}) 访问码错误", userName, userId);
            bot.sendPrivateMsg(userId, "❌访问码错误", false);
        } else {
            // 私聊对话处理
            String parsed = MsgUtil.formatMsg(bot, event.getArrayMsg());
            log.info("◉ [PrivateAction:Chat] 私聊 {}({}) -> {}", userName, userId, parsed);
            cmdProcessor.processQQ(bot, CmdEvent.of(
                    event, "Chat", List.of(parsed), false, false));
        }
    }

    @FuncControl("PrivateCmd")
    @PrivatePokeNoticeHandler
    @Async("ThreadExecutor")
    public void onPrivatePokeInteraction(Bot bot, PokeNoticeEvent event) throws Exception {
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [PrivateAction:Poke] 私聊 -> From {} to {} (仅戳Bot)", event.getUserId(), event.getTargetId());
            cmdProcessor.processQQ(bot, CmdEvent.of(event));
        }
    }

    // ================================== 群聊动作捕获 ==================================

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NOT_NEED)
    @Async("ThreadExecutor")
    public void onGroupMessageInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 指令输入捕获
        if (botMonitor.doGroupInputResponse(event)) {
            botMonitor.doGroupMsgCollect(bot, event);
            botMonitor.doGroupImgCollect(event);
            return;
        }
        // 串行调用 消息预处理 默认处理情况
        botMonitor.doGroupKeywordAct(bot, event);
        if (!botMonitor.doGroupAIAutoReply(bot, event))  // 按需 AI自动记录
            botMonitor.doGroupMsgCollect(bot, event);
        botMonitor.doGroupImgCollect(event);
        botMonitor.doGroupBottleAutoThrow(bot, event);

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String message = event.getMessage();

        if (message.startsWith(cmdProperties.getPrefix())) {
            // 普通命令处理
            log.info("◉ [GroupAction:Cmd] 群聊 {} - {}({}) -> {}", groupId, userName, userId, message);
            cmdProcessor.processQQ(bot, CmdEvent.of(event));
        } else if (event.getArrayMsg().size() > 1 && event.getArrayMsg().get(0).getType() == MsgTypeEnum.reply) {
            // 引用命令处理
            if (!event.getArrayMsg().get(1).getStringData("text").startsWith(cmdProperties.getPrefix())) return;
            log.info("◉ [GroupAction:ReplyCmd] 群聊 {} - {}({}) -> {}", groupId, userName, userId, message);
            cmdProcessor.processQQ(bot, CmdEvent.of(event));
        }
    }

    @FuncControl("PokeDetect")
    @GroupPokeNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupPokeInteraction(Bot bot, PokeNoticeEvent event) throws Exception {
        if (!BotCtx.getSetting().isPokeDetect()) return;
        if (Objects.equals(event.getTargetId(), event.getSelfId())) {
            log.info("◉ [GroupAction:Poke] 群聊 {} -> From {} to {} (仅戳Bot)", event.getGroupId(), event.getUserId(), event.getTargetId());
            cmdProcessor.processQQ(bot, CmdEvent.of(event));
        }
    }

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    @Async("ThreadExecutor")
    public void onGroupAtInteraction(Bot bot, GroupMessageEvent event) throws Exception {

        // 串行调用 消息预处理 默认处理情况
        // botMonitor.doGroupKeywordAct(bot, event);  // 禁用 关键词检测
        // if (!botMonitor.doGroupAIAutoReply(bot, event))  // 无需 AI即将回复
        //     botMonitor.doGroupMsgCollect(bot, event);  // 无需 AI自动记录
        botMonitor.doGroupImgCollect(event);
        botMonitor.doGroupBottleAutoThrow(bot, event);

        String parsed = MsgUtil.formatMsg(bot, event.getArrayMsg());
        log.info("◉ [GroupAction:At] 群聊 {} - {}({}) -> {}",
                event.getGroupId(), event.getSender().getNickname(), event.getUserId(), parsed);
        cmdProcessor.processQQ(bot, CmdEvent.of(
                event, "Chat", List.of(parsed), true, true));
    }

    @FuncControl("RecallDetect")
    @GroupMsgDeleteNoticeHandler
    @Async("ThreadExecutor")
    public void onGroupRecallInteraction(Bot bot, GroupMsgDeleteNoticeEvent event) throws Exception {
        if (!BotCtx.getSetting().isRecallDetect()) return;
        log.info("◉ [GroupAction:Recall] 群聊 {} -> {}", event.getGroupId(), event.getUserId());
        cmdProcessor.processQQ(bot, CmdEvent.of(event));
    }
}