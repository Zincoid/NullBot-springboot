package org.bot.nullbot.util;

public final class BotCtxUtil {

    public static final ThreadLocal<String> userId = new ThreadLocal<>();
    public static final ThreadLocal<String> groupId = new ThreadLocal<>();
    public static final ThreadLocal<String> setting = new ThreadLocal<>();

    private BotCtxUtil() {}

    public static void set(String userId, String groupId, String setting) {
        setUserId(userId);
        setGroupId(groupId);
        setSetting(setting);
    }

    public static void setUserId(String userId) {
        BotCtxUtil.userId.set(userId);
    }
    public static void setGroupId(String groupId) {
        BotCtxUtil.groupId.set(groupId);
    }
    public static void setSetting(String setting) {
        BotCtxUtil.setting.set(setting);
    }

    public static String getUserId() {
        return userId.get();
    }
    public static String getGroupId() {
        return groupId.get();
    }
    public static String getSetting() {
        return setting.get();
    }

    public static void remove() {
        userId.remove();
        groupId.remove();
        setting.remove();
    }
}
