package org.bot.nullbot.entity.setting;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.enums.LimitScope;

@Data
@RequiredArgsConstructor
public class LimitOption {

    private final Long groupId;

    private LimitScope limitScope = LimitScope.Group;
    private int limitCapacity = 25;
    private int limitRefill = 10;
    private int limitInterval = 1;

    public LimitScope switchLimitScope() {
        return this.limitScope = this.limitScope.next();
    }
}
