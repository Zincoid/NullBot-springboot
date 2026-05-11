package org.bot.nullbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.enums.ChatScope;

@Data
@AllArgsConstructor
public class ChatOption {
    private ChatScope chatScope;
    private boolean antiInjection;
    private boolean thinking;
    private boolean voice;
    private boolean embedding;
    private boolean embeddingAuth;
    private boolean custom;
}
