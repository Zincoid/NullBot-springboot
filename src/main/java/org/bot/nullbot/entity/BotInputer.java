package org.bot.nullbot.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.component.control.BotInputManager;
import org.bot.nullbot.enums.BniMode;

import java.util.List;

@Data
@RequiredArgsConstructor
public class BotInputer {

    private final BniMode mode;
    private final Long targetId;

    private String pattern = ".*";
    private long timeout = 30;
    private boolean coverable = false;

    public BotInputer pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public BotInputer timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public BotInputer coverable() {
        this.coverable = true;
        return this;
    }

    public List<Pair<Long, String>> next() {
        return BotInputManager.request(mode, targetId, pattern, timeout, coverable);
    }
}
