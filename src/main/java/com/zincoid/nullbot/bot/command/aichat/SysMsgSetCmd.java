package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.control.SysMsgManager;
import com.zincoid.nullbot.core.service.base.UserService;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"SysMsgSet", "提示词设置", "提示词"})
@Component
public class SysMsgSetCmd implements Cmd {

    private final QQChatClient qqChatClient;
    private final SysMsgManager sysMsgManager;
    private final UserService userService;

    public SysMsgSetCmd(@Lazy QQChatClient qqChatClient, SysMsgManager sysMsgManager, UserService userService) {
        this.qqChatClient = qqChatClient;
        this.sysMsgManager = sysMsgManager;
        this.userService = userService;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        if (args.hasOpt("view", "v")) {
            String message = "ℹ️当前提示词: %s".formatted(
                    sysMsgManager.getGroupMessage(groupId));
            bot.sendGroupMsg(groupId, message, false);
            log.info("☑ [SysMsgSet] 群聊提示词已查看 -> {}", groupId);
            return;
        }
        int userAccess = userService.getAccess(userId);
        if (userAccess < 1 && !BotCtx.getSetting().isCustom()) {
            bot.sendGroupMsg(groupId, """
                        🚫提示词操作被阻止
                        - 非自定模式: 需限权I及以上
                        - 你的限权: %s""".formatted(userAccess), false);
            return;
        }
        if (args.hasOpt("reset", "r")) {
            sysMsgManager.resetGroup(groupId);
            qqChatClient.clear(BotCtx.getChatId());
            bot.sendGroupMsg(groupId, "✅提示词已重置", false);
            log.info("☑ [SysMsgSet] 群聊提示词已重置 -> {}", groupId);
            return;
        }
        if (args.hasOpt("set", "s")) {
            String newMessage = args.rest();
            sysMsgManager.setGroupMessage(groupId, newMessage);
            qqChatClient.clear(BotCtx.getChatId());
            bot.sendGroupMsg(groupId, "✅提示词已设置", false);
            log.info("☑ [SysMsgSet] 群聊提示词已设置 - {} -> {}", groupId, newMessage);
            return;
        }
        throw new BotWarnException("无此操作");
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        Long userId = event.getUserId();

        if (args.hasOpt("view", "v")) {
            String message = "ℹ️当前提示词: %s".formatted(
                    sysMsgManager.getUserMessage(userId));
            bot.sendPrivateMsg(userId, message, false);
            log.info("☑ [SysMsgSet] 私聊提示词已查看 -> {}", userId);
            return;
        }
        if (args.hasOpt("reset", "r")) {
            sysMsgManager.resetUser(userId);
            qqChatClient.clear(BotCtx.getChatId());
            bot.sendPrivateMsg(userId, "[提示词设置] ✅已重置", false);
            log.info("☑ [SysMsgSet] 私聊提示词已重置 -> {}", userId);
            return;
        }
        if (args.hasOpt("set", "s")) {
            String newMessage = args.rest();
            sysMsgManager.setUserMessage(userId, newMessage);
            qqChatClient.clear(BotCtx.getChatId());
            bot.sendPrivateMsg(userId, "[提示词设置] ✅已设置", false);
            log.info("☑ [SysMsgSet] 私聊提示词已设置 - {} -> {}", userId, newMessage);
            return;
        }
        throw new BotWarnException("无此操作");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SysMsgSet 命令
                功能: 设置AI提示词并重置历史
                限权: %d 级
                格式: SysMsgSet [选项]

                选项:
                -s,--set [文本]  设置提示词
                -r,--reset      重置提示词
                -v,--view       查看提示词

                别名: 提示词设置/提示词
                注意:
                - 非Custom模式 变更需限权I及以上
                - Custom模式 变更需限权0及以上""", getAccess()
        );
    }
}
