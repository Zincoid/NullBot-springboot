package com.zincoid.nullbot.core.context;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.enums.ChatScope;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;

public final class BotCtx {

    private static final ThreadLocal<Bot> BOT = new ThreadLocal<>();
    private static final ThreadLocal<Event> EVENT = new ThreadLocal<>();
    private static final ThreadLocal<EventScope> SCOPE = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> GROUP_ID = new ThreadLocal<>();
    private static final ThreadLocal<SettingPO> SETTING = new ThreadLocal<>();

    private BotCtx() {}

    // =================== 系统资源方法 ===================

    public static void setCore(Bot bot, Event event) {
        BotCtx.BOT.set(bot);
        BotCtx.EVENT.set(event);
    }
    public static Bot getBot() {
        return BOT.get();
    }
    public static Event getEvent() {
        return EVENT.get();
    }

    // =================== 基本数据方法 ===================

    public static void setGroup(Long userId, Long groupId, SettingPO setting) {
        BotCtx.SCOPE.set(EventScope.GROUP);
        BotCtx.USER_ID.set(userId);
        BotCtx.GROUP_ID.set(groupId);
        BotCtx.SETTING.set(setting);
    }
    public static void setPrivate(Long userId) {
        BotCtx.SCOPE.set(EventScope.PRIVATE);
        BotCtx.USER_ID.set(userId);
        BotCtx.GROUP_ID.set(0L);
        BotCtx.SETTING.remove();
    }
    public static void setUnknown() {
        BotCtx.SCOPE.set(EventScope.UNKNOWN);
        BotCtx.USER_ID.remove();
        BotCtx.GROUP_ID.remove();
        BotCtx.SETTING.remove();
    }

    public static EventScope getScope() {
        return SCOPE.get();
    }
    public static Long getUserId() {
        return USER_ID.get();
    }
    public static Long getGroupId() {
        return GROUP_ID.get();
    }
    public static SettingPO getSetting() {
        return SETTING.get();
    }

    // =================== 清除数据方法 ===================

    public static void remove() {
        BOT.remove();
        EVENT.remove();
        SCOPE.remove();
        USER_ID.remove();
        GROUP_ID.remove();
        SETTING.remove();
    }

    // =================== 应用工具方法 ===================

    public static String getChatId() {
        if (getScope() == EventScope.PRIVATE) return "Private_" + getUserId();
        ChatScope chatScope = getSetting().getChatScope();
        return chatScope + "_" + (chatScope == ChatScope.PERSONAL
                ? getUserId() : getGroupId());
    }
}
