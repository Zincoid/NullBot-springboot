package com.zincoid.nullbot.core.context;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;

public final class BotCtx {

    private static final ThreadLocal<Bot> bot = new ThreadLocal<>();
    private static final ThreadLocal<Event> event = new ThreadLocal<>();
    private static final ThreadLocal<EventScope> scope = new ThreadLocal<>();
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Long> groupId = new ThreadLocal<>();
    private static final ThreadLocal<SettingPO> setting = new ThreadLocal<>();

    private BotCtx() {}

    // =================== 系统资源方法 ===================

    public static void setCore(Bot bot, Event event) {
        BotCtx.bot.set(bot);
        BotCtx.event.set(event);
    }
    public static Bot getBot() {
        return bot.get();
    }
    public static Event getEvent() {
        return event.get();
    }

    // =================== 基本数据方法 ===================

    public static void setGroup(Long userId, Long groupId, SettingPO setting) {
        BotCtx.scope.set(EventScope.GROUP);
        BotCtx.userId.set(userId);
        BotCtx.groupId.set(groupId);
        BotCtx.setting.set(setting);
    }
    public static void setPrivate(Long userId) {
        BotCtx.scope.set(EventScope.PRIVATE);
        BotCtx.userId.set(userId);
        BotCtx.groupId.set(0L);
        BotCtx.setting.remove();
    }
    public static void setUnknown() {
        BotCtx.scope.set(EventScope.UNKNOWN);
        BotCtx.userId.remove();
        BotCtx.groupId.remove();
        BotCtx.setting.remove();
    }

    public static EventScope getScope() {
        return scope.get();
    }
    public static Long getUserId() {
        return userId.get();
    }
    public static Long getGroupId() {
        return groupId.get();
    }
    public static SettingPO getSetting() {
        return setting.get();
    }

    // =================== 清除数据方法 ===================

    public static void remove() {
        bot.remove();
        event.remove();
        scope.remove();
        userId.remove();
        groupId.remove();
        setting.remove();
    }

    // =================== 应用工具方法 ===================

    public static String getChatId() {
        if (getScope() == EventScope.PRIVATE) return "Private_" + getUserId();
        ChatScope chatScope = getSetting().getChatScope();
        return chatScope + "_" + (chatScope == ChatScope.PERSONAL
                ? getUserId() : getGroupId());
    }
}
