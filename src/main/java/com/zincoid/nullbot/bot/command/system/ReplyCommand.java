package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.component.ai.voice.TtsClient;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Reply", "回复"})
@Component
@RequiredArgsConstructor
public class ReplyCommand implements Command {

    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        String message = params.nextFullString();
        String content = BotCtxUtil.getSetting().isVoice() ?
                MsgUtils.builder().voice("base64://" + ttsClient.synthesize(message)).build() : message;
        bot.sendGroupMsg(event.getGroupId(), content, false);
        log.info("☑ [Reply] 群聊已回复: {}", message);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        String message = params.nextFullString();
        bot.sendPrivateMsg(event.getUserId(), message, false);
        log.info("☑ [Reply] 私聊已回复: {}", message);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reply 命令
                功能: 文本输出 (用于AI工具调用模式中间回复)
                限权: %d 级
                格式: Reply [内容]
                别名: 回复
                注意：
                1. 仅在工具调用模式下可使用
                2. 需要在回复文本后继续执行工具调用时，使用该指令进行文本回复
                3. 此次回复会视作一次工具调用，对话因此不会中断""", getAccess()
        );
    }
}
