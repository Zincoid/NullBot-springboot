package com.zincoid.nullbot.bot.interactor;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.enums.BniMode;

import java.util.List;

@Data
@RequiredArgsConstructor
public class BotInputer {

    private final Long targetId;

    private BniMode mode = BniMode.PS;
    private String pattern = ".*";
    private long timeout = 30;
    private boolean coverable = false;

    public BotInputer mode(BniMode mode) {
        this.mode = mode;
        return this;
    }
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
        return BotInputManager.register(this);
    }
}
