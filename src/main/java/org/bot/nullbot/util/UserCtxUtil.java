package org.bot.nullbot.util;

public class UserCtxUtil {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Integer> userType = new ThreadLocal<>();

    private UserCtxUtil() {}

    public static void set(Long id, Integer type) {
        setId(id);
        setType(type);
    }

    public static void setId(Long id) {
        userId.set(id);
    }

    public static void setType(Integer type) {
        userType.set(type);
    }

    public static Long getId() {
        return userId.get();
    }
    public static Integer getType() {
        return userType.get();
    }

    public static void remove() {
        userId.remove();
        userType.remove();
    }
}
