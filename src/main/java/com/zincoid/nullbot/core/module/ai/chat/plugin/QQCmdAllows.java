package com.zincoid.nullbot.core.module.ai.chat.plugin;

import java.util.HashSet;
import java.util.Set;

public class QQCmdAllows {

    private static final Set<String> GC_CMD_ALLOWS;
    private static final Set<String> PM_CMD_ALLOWS;

    static {

        GC_CMD_ALLOWS = Set.of(
                /* ========== 中间回复 ========== */
                "Reply",
                /* ========== 普通命令 ========== */
                "aud", "vid", "img", "say",
                "UserBan",
                "Help", "ImageFolder", "PUBG",
                "Anime", "OneTimeAlarm",
                /* ========== 合成命令 ========== */
                "Convert", "Symmetry",
                /* ========== 加密命令 ========== */
                "eb0f8545", "4ed1314d", "65275d24",
                "1e7bd161", "b6713262", "db3fbe2b",
                "0167a25a", "bab329aa", "1a0d3829",
                "a7b3c9d1"
        );

        PM_CMD_ALLOWS = Set.of(
                /* ========== 中间回复 ========== */
                "Reply",
                /* ========== 普通命令 ========== */
                "Help",
                /* ========== 合成命令 ========== */
                /* ========== 加密命令 ========== */
                "65275d24", "0167a25a", "bab329aa",
                "1a0d3829", "a7b3c9d1"
        );

    }

    public static Set<String> getGc() {
        return GC_CMD_ALLOWS;
    }

    public static Set<String> getPm() {
        return PM_CMD_ALLOWS;
    }

    public static Set<String> getAll() {
        Set<String> all = new HashSet<>();
        all.addAll(GC_CMD_ALLOWS);
        all.addAll(PM_CMD_ALLOWS);
        return all;
    }
}
