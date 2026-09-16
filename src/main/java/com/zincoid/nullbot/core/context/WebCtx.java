package com.zincoid.nullbot.core.context;

import com.zincoid.nullbot.core.exception.CoreException;

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

    public static void requireAdmin() {
        if (getType() == null)
            throw new CoreException("未登录");
        if (getType() != 1)
            throw new CoreException("访客受限");
    }

    public static void remove() {
        USER_ID.remove();
        USER_TYPE.remove();
    }
}
