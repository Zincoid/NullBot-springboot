package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.ai.tts.TtsClient;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Reply", "回复"})
@Component
@RequiredArgsConstructor
public class ReplyCmd implements Cmd {

    private final TtsClient ttsClient;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String message = args.rest();
        String content = BotCtx.getSetting().isVoice() ?
                MsgUtils.builder().voice("base64://" + ttsClient.synthesize(message)).build() : message;
        bot.sendGroupMsg(event.getGroupId(), content, false);
        log.info("☑ [Reply] 群聊已回复: {}", message);
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        String message = args.rest();
        bot.sendPrivateMsg(event.getUserId(), message, false);
        log.info("☑ [Reply] 私聊已回复: {}", message);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reply 命令
                功能: 聊天文本输出
                限权: %d 级
                格式: Reply [文本]
                别名: 回复
                注意：
                - 仅在工具调用模式下可使用, 用于AI工具调用模式中间回复
                - 需要在回复文本后继续执行工具调用时, 使用该指令进行文本回复
                - 此次回复会视作一次工具调用, 对话因此不会中断""", getAccess()
        );
    }
}
