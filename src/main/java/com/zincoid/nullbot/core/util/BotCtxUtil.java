package com.zincoid.nullbot.core.util;

import com.zincoid.nullbot.core.entity.po.SettingPO;

public final class BotCtxUtil {

    public static final ThreadLocal<Long> userId = new ThreadLocal<>();
    public static final ThreadLocal<Long> groupId = new ThreadLocal<>();
    public static final ThreadLocal<SettingPO> setting = new ThreadLocal<>();

    private BotCtxUtil() {}

    public static void set(Long userId, Long groupId, SettingPO setting) {
        setUserId(userId);
        setGroupId(groupId);
        setSetting(setting);
    }

    public static void setUserId(Long userId) {
        BotCtxUtil.userId.set(userId);
    }
    public static void setGroupId(Long groupId) {
        BotCtxUtil.groupId.set(groupId);
    }
    public static void setSetting(SettingPO setting) {
        BotCtxUtil.setting.set(setting);
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

    public static void remove() {
        userId.remove();
        groupId.remove();
        setting.remove();
    }
}
