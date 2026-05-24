package com.zincoid.nullbot.core.component.ai.chat.plugin;

import java.util.Set;

public class QQCmdAllows {

    private static final Set<String> GC_CMD_ALLOWS;
    private static final Set<String> PM_CMD_ALLOWS;

    static {

        GC_CMD_ALLOWS = Set.of(
                /* ========== 普通命令 ========== */
                "aud", "vid", "img", "say",
                "ChatReset", "UserBan",
                "Help", "ImageFolder", "PUBG",
                "Anime", "OneTimeAlarm",
                /* ========== 合成命令 ========== */
                "Convert", "Symmetry", "Tts",
                /* ========== 加密命令 ========== */
                "eb0f8545", "4ed1314d", "65275d24",
                "1e7bd161", "b6713262", "db3fbe2b",
                "0167a25a", "bab329aa"
        );

        PM_CMD_ALLOWS = Set.of(
                /* ========== 普通命令 ========== */
                "Help",
                /* ========== 合成命令 ========== */
                "Tts",
                /* ========== 加密命令 ========== */
                "65275d24", "0167a25a", "bab329aa"
        );

    }

    public static Set<String> getGc() {
        return GC_CMD_ALLOWS;
    }

    public static Set<String> getPm() {
        return PM_CMD_ALLOWS;
    }
}
