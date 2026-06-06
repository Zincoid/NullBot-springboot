package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.SysMsgManager;
import com.zincoid.nullbot.core.service.basic.UserService;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"SysMsgSet", "提示词设置", "提示词"})
@Component
public class SysMsgSetCommand implements Command {

    private final QQAiClient qqAiClient;
    private final SysMsgManager sysMsgManager;
    private final UserService userService;

    public SysMsgSetCommand(@Lazy QQAiClient qqAiClient, SysMsgManager sysMsgManager, UserService userService) {
        this.qqAiClient = qqAiClient;
        this.sysMsgManager = sysMsgManager;
        this.userService = userService;
    }

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String option = args.nextString();

        if ("-view".equals(option)) {
            String message = "ℹ️当前提示词: %s".formatted(
                    sysMsgManager.getGroupMessage(groupId));
            bot.sendGroupMsg(groupId, message, false);
            log.info("☑ [SysMsgSet] 群聊提示词已查看 -> {}", groupId);
            return;
        }
        int userAccess = userService.getAccess(userId);
        if (userAccess < 1 && !BotCtxUtil.getSetting().isCustom()) {
            bot.sendGroupMsg(groupId, """
                        🚫提示词操作被阻止
                        - 当前为非自定模式
                        - 操作需限权I及以上
                        - 你的限权等级: %s""".formatted(userAccess), false);
            return;
        }
        if ("-reset".equals(option)) {
            sysMsgManager.resetGroup(groupId);
            qqAiClient.clear(BotCtxUtil.getChatId());
            bot.sendGroupMsg(groupId, "✅提示词已重置", false);
            log.info("☑ [SysMsgSet] 群聊提示词已重置 -> {}", groupId);
            return;
        }
        if ("-set".equals(option)) {
            String newMessage = args.nextFullString();
            sysMsgManager.setGroupMessage(groupId, newMessage);
            qqAiClient.clear(BotCtxUtil.getChatId());
            bot.sendGroupMsg(groupId, "✅提示词已设置", false);
            log.info("☑ [SysMsgSet] 群聊提示词已设置 - {} -> {}", groupId, newMessage);
            return;
        }
        throw new BotWarnException("无此操作");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) {
        Long userId = event.getUserId();
        String option = args.nextString();

        if ("-view".equals(option)) {
            String message = "ℹ️当前提示词: %s".formatted(
                    sysMsgManager.getUserMessage(userId));
            bot.sendPrivateMsg(userId, message, false);
            log.info("☑ [SysMsgSet] 私聊提示词已查看 -> {}", userId);
            return;
        }
        if ("-reset".equals(option)) {
            sysMsgManager.resetUser(userId);
            qqAiClient.clear(BotCtxUtil.getChatId());
            bot.sendPrivateMsg(userId, "[提示词设置] ✅已重置", false);
            log.info("☑ [SysMsgSet] 私聊提示词已重置 -> {}", userId);
            return;
        }
        if ("-set".equals(option)) {
            String newMessage = args.nextFullString();
            sysMsgManager.setUserMessage(userId, newMessage);
            qqAiClient.clear(BotCtxUtil.getChatId());
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
                功能: 设置AI系统提示词并清空历史
                限权: %d 级
                格式:
                1. SysMsgSet [-view]
                2. SysMsgSet [-set] [提示词]
                3. SysMsgSet [-reset]
                别名: 提示词设置/提示词
                注意:
                - 非Custom模式 变更需限权I及以上
                - Custom模式 变更需限权0及以上""", getAccess()
        );
    }
}
