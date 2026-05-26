package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.SysMsgManager;
import com.zincoid.nullbot.core.service.UserService;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"SysMsgSet", "提示词设置"})
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
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String option = params.nextString();

        if ("-reset".equals(option)) {
            int userAccess = userService.getAccess(userId);
            if (userAccess < 1)
                throw new NullBotException("""
                        重置失败
                        - 仅限权等级I及以上用户可重置提示词
                        - 你的限权等级: %s""".formatted(userAccess));
            qqAiClient.clear(BotCtxUtil.getChatId());
            sysMsgManager.resetGroup(groupId);
            bot.sendGroupMsg(groupId, "[提示词设置] ✅已重置", false);
            log.info("├─[SysMsgSet] 群聊提示词已重置 - {}", groupId);
            return;
        }

        if ("-set".equals(option)) {
            int userAccess = userService.getAccess(userId);
            if (userAccess < 1 && !BotCtxUtil.getSetting().isCustom())
                throw new NullBotException("""
                        设置失败
                        - 当前为非自定义提示词模式
                        - 该模式仅限权等级I及以上用户可修改
                        - 你的限权等级: %s""".formatted(userAccess));
            String newMessage = params.nextFullString();
            qqAiClient.clear(BotCtxUtil.getChatId());
            sysMsgManager.setGroupMessage(groupId, newMessage);
            bot.sendGroupMsg(groupId, "[提示词设置] ✅已设置", false);
            log.info("☑ [SysMsgSet] 群聊提示词已设置 - {} -> {}", groupId, newMessage);
            return;
        }

        throw new NullBotException("无此操作");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        Long userId = event.getUserId();
        String option = params.nextString();

        if ("-reset".equals(option)) {
            qqAiClient.clear(BotCtxUtil.getChatId());
            sysMsgManager.resetUser(userId);
            bot.sendPrivateMsg(userId, "[提示词设置] ✅已重置", false);
            log.info("☑ [SysMsgSet] 私聊提示词已重置 - {}", userId);
            return;
        }

        if (params.size() < 2)
            throw new NullBotException("参数不足");

        if ("-set".equals(option)) {
            String newMessage = params.nextFullString();
            qqAiClient.clear(BotCtxUtil.getChatId());
            sysMsgManager.setUserMessage(userId, newMessage);
            bot.sendPrivateMsg(userId, "[提示词设置] ✅已设置", false);
            log.info("☑ [SysMsgSet] 私聊提示词已设置 - {} -> {}", userId, newMessage);
            return;
        }

        throw new NullBotException("无此操作");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SysMsgSet 命令
                功能: 设置AI系统提示词并清空历史 (部分操作需二次限权验证)
                限权: %d 级
                格式:
                1. SysMsgSet [-reset]
                2. SysMsgSet [-default|-custom] [提示词]
                别名: 提示词设置
                注意:
                - 模式切换 使用 GroupSet 群设置指令
                - Reset操作 仅限权I及以上可重置全部提示词
                - Default模式 仅限权I及以上可修改提示词
                - Custom模式 仅限权0及以上可修改提示词
                - Custom模式 默认禁用指令模式""", getAccess()
        );
    }
}
