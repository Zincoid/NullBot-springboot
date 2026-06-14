package com.zincoid.nullbot.core.module.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotOperator {

    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final int DEFAULT_RETRY_INTERVAL = 1000;

    @Value("${bot.bot-id}")
    private Long botId;
    @Value("${bot.log-id}")
    private Long logId;

    private final BotContainer botContainer;

    // =================== 获取方法 ===================

    public Bot getBot(int maxRetries, long retryInterval) {
        Bot bot = BotCtx.getBot();
        if (bot != null) return bot;
        for (int i = 0; i < maxRetries; i++) {
            bot = botContainer.robots.get(botId);
            if (bot != null) return bot;
            log.info("▽ [BotOperator] 获取Bot失败({}/{}): 将于 {}ms 后重试", i + 1, maxRetries, retryInterval);
            sleep(retryInterval);
        }
        throw new RuntimeException("BotOperator 错误: Bot 获取重试超出最大次数");
    }

    public Bot getBot() {
        return getBot(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    // =================== 默认方法 ===================

    public void sendLogGroupMsg(String message) {
        sendGroupMsg(logId ,message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public void sendAllGroupMsg(String message) {
        sendAllGroupMsg(message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public Integer sendGroupMsg(Long groupId, String message) {
        return sendGroupMsg(groupId, message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public Integer sendPrivateMsg(Long userId, String message) {
        return sendPrivateMsg(userId, message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    // =================== 消息方法 ===================

    public void sendAllGroupMsg(String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        for (GroupInfoResp group : bot.getGroupList().getData())
            bot.sendGroupMsg(group.getGroupId(), message, false);
        log.info("▽ [BotOperator] 全群消息已发送: {}", message);
    }

    public Integer sendGroupMsg(Long groupId, String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        ActionData<MsgId> actionData = bot.sendGroupMsg(groupId, message, false);
        log.info("▽ [BotOperator] 群聊({})消息已发送: {}", groupId, message);
        return actionData.getData().getMessageId();
    }

    public Integer sendPrivateMsg(Long userId, String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        ActionData<MsgId> actionData = bot.sendPrivateMsg(userId, message, false);
        log.info("▽ [BotOperator] 私聊({})消息已发送: {}", userId, message);
        return actionData.getData().getMessageId();
    }

    // =================== 工具方法 ===================

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("BotOperator 错误: 中断异常", e);
        }
    }
}
