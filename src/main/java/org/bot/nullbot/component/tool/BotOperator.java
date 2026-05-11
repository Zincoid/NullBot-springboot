package org.bot.nullbot.component.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotOperator {

    @Value("${nullbot.bot-id}")
    private Long botId;
    @Value("${nullbot.log-id}")
    private Long logId;

    private final BotContainer botContainer;

    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final int DEFAULT_RETRY_INTERVAL = 1000;

    // =================== 获取方法 ===================

    public Bot getBot(int maxRetries, long retryInterval) {
        int retryCount = 0;
        Bot bot = botContainer.robots.get(botId);
        while (bot == null && retryCount < maxRetries) {
            retryCount++;
            log.info("▽ [BotOperator] 获取Bot失败({}/{}): 将于 {}ms 后重试", retryCount, maxRetries, retryInterval);
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException("BotOperator 错误: 中断异常", e);
            }
            bot = botContainer.robots.get(botId);
        }
        if (bot == null) throw new RuntimeException("BotOperator 错误: Bot 获取重试超出最大次数");
        return bot;
    }

    // =================== 默认方法 ===================

    public void sendLogGroupMsg(String message) {
        sendGroupMsg(logId ,message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public void sendAllGroupMsg(String message) {
        sendAllGroupMsg(message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public void sendGroupMsg(Long groupId, String message) {
        sendGroupMsg(groupId, message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    public void sendPrivateMsg(Long userId, String message) {
        sendPrivateMsg(userId, message, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL);
    }

    // =================== 消息方法 ===================

    public void sendAllGroupMsg(String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        for (GroupInfoResp group : bot.getGroupList().getData())
            bot.sendGroupMsg(group.getGroupId(), message, false);
        log.info("▽ [BotOperator] 全群消息已发送: {}", message);
    }

    public void sendGroupMsg(Long groupId, String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        bot.sendGroupMsg(groupId, message, false);
        log.info("▽ [BotOperator] 群聊({})消息已发送: {}", groupId, message);
    }

    public void sendPrivateMsg(Long userId, String message, int maxRetries, long retryInterval) {
        Bot bot = getBot(maxRetries, retryInterval);
        bot.sendPrivateMsg(userId, message, false);
        log.info("▽ [BotOperator] 私聊({})消息已发送: {}", userId, message);
    }
}
