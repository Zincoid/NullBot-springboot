package org.bot.nullbot.entity.setting;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.enums.ChatScope;

@Data
@RequiredArgsConstructor
public class ChatOption {

    private final Long groupId;

    private ChatScope chatScope = ChatScope.Group;
    private boolean antiInjection = true;
    private boolean thinking = false;
    private boolean voice = false;
    private boolean embedding = true;
    private boolean embeddingAuth = false;
    private boolean custom = false;

    private boolean autoReply = false;
    private double replyFrequency = 0.001;

    public ChatScope switchChatScope() {
        return chatScope = chatScope.next();
    }

    public boolean switchAntiInjection() {
        return antiInjection = !antiInjection;
    }

    public boolean switchThinking() {
        return thinking = !thinking;
    }

    public boolean switchVoice() {
        return voice = !voice;
    }

    public boolean switchEmbedding() {
        return embedding = !embedding;
    }

    public boolean switchEmbeddingAuth() {
        return embeddingAuth = !embeddingAuth;
    }

    public boolean switchCustom() {
        return custom = !custom;
    }

    public boolean switchAutoReply() {
        return autoReply = !autoReply;
    }
}
