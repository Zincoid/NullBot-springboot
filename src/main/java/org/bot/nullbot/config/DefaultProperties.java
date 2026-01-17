package org.bot.nullbot.config;

import lombok.Data;
import org.bot.nullbot.enums.Scope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.default")
public class DefaultProperties
{
    private Scope scope;  // 会话范围
    private boolean antiInjection;  // 防注入模式
    private boolean thinking;  // 深度思考模式
    private boolean voice;  // 语音模式
    private boolean embedding;  // 嵌入命令模式
    private boolean embeddingAuth;  // 嵌入限权验证
    private boolean custom;  // 自定义提示词模式
    private boolean autoReply;  // 自动回复模式
    private double replyFrequency;  // 猜 切割比例


    private boolean imageCollect;  // 图片收集
    private boolean messageCollect;  // 消息收集
    private boolean keywordDetect;  // 关键词检测
    private boolean pokeDetect;  // 戳一戳检测
    private boolean recallDetect;  // 防撤回

    private double guessRatio;  // 猜 切割比例
    private int guessPadding;  // 猜 内边距
}
