package org.bot.nullbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.enums.Scope;

@Data
@AllArgsConstructor
public class ChatOption
{
    private Scope scope;
    private boolean antiInjection;
    private boolean thinking;
    private boolean embedding;
    private boolean embeddingAuth;
    private boolean custom;
}
