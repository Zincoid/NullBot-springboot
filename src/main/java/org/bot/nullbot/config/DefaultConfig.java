package org.bot.nullbot.config;

import lombok.Data;
import org.bot.nullbot.enums.Scope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.default")
public class DefaultConfig
{
    private Scope scope;  // 会话范围
    private boolean antiInjection;  // 防注入模式
    private boolean thinking;  // 深度思考模式
    private boolean embedding;  // 嵌入命令模式
    private boolean embeddingAuth;  // 嵌入限权验证

    private Boolean imageCollect;  // 图片收集
    private Boolean messageCollect;  // 消息收集
    private Boolean keywordDetect;  // 关键词检测
    private Boolean pokeDetect;  // 戳一戳检测
    private Boolean recallDetect;  // 防撤回

    private Double guessRatio;  // 猜 切割比例
    private Integer guessPadding;  // 猜 内边距
}
