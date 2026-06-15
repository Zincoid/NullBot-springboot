package com.zincoid.nullbot.core.context;

public final class WebCtx {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();

    private WebCtx() {}

    public static void set(Long id, Integer type) {
        setId(id);
        setType(type);
    }

    public static void setId(Long id) {
        USER_ID.set(id);
    }
    public static void setType(Integer type) {
        USER_TYPE.set(type);
    }

    public static Long getId() {
        return USER_ID.get();
    }
    public static Integer getType() {
        return USER_TYPE.get();
    }

    public static void remove() {
        USER_ID.remove();
        USER_TYPE.remove();
    }
}
