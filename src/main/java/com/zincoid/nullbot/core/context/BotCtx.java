package com.zincoid.nullbot.core.context;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;

public final class BotCtx {

    private static final ThreadLocal<Bot> bot = new ThreadLocal<>();
    private static final ThreadLocal<Event> event = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> isPrivate = new ThreadLocal<>();
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Long> groupId = new ThreadLocal<>();
    private static final ThreadLocal<SettingPO> setting = new ThreadLocal<>();

    private BotCtx() {}

    // =================== 系统资源方法 ===================

    public static void setBot(Bot bot) {
        BotCtx.bot.set(bot);
    }
    public static void setEvent(Event event) {
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
        setIsPrivate(false);
        setUserId(userId);
        setGroupId(groupId);
        setSetting(setting);
    }
    public static void setPrivate(Long userId) {
        setIsPrivate(true);
        setUserId(userId);
    }

    public static void setIsPrivate(boolean isPrivate) {
        BotCtx.isPrivate.set(isPrivate);
    }
    public static void setUserId(Long userId) {
        BotCtx.userId.set(userId);
    }
    public static void setGroupId(Long groupId) {
        BotCtx.groupId.set(groupId);
    }
    public static void setSetting(SettingPO setting) {
        BotCtx.setting.set(setting);
    }

    public static boolean getIsPrivate() {
        return isPrivate.get();
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
        isPrivate.remove();
        userId.remove();
        groupId.remove();
        setting.remove();
    }

    // =================== 应用方法 ===================

    public static String getChatId() {
        if (getIsPrivate()) return "Private_" + getUserId();
        ChatScope scope = getSetting().getChatScope();
        return scope + "_" + (scope == ChatScope.PERSONAL
                ? getUserId() : getGroupId());
    }
}
