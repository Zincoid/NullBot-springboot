package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.ai.chat.manage.AiCostManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Recover", "恢复"})
@Component
@RequiredArgsConstructor
public class RecoverCmd implements Cmd {

    private final AiCostManager aiCostManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        aiCostManager.recover();
        bot.sendGroupMsg(event.getGroupId(), "✅AI欠费状态已重置", false);
        log.info("☑ [Recover] AI欠费状态已重置 - Group: {}", event.getGroupId());
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        aiCostManager.recover();
        bot.sendPrivateMsg(event.getUserId(), "✅AI欠费状态已重置", false);
        log.info("☑ [Recover] AI欠费状态已重置 - User: {}", event.getUserId());
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Recover 命令
                功能: 重置AI欠费状态以恢复AI对话指令
                限权: %d 级
                格式: Recover
                别名: 恢复""", getAccess()
        );
    }
}
