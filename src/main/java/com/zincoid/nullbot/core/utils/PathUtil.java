package com.zincoid.nullbot.core.utils;

public final class PathUtil {

    private PathUtil() {}

    public static String join(String directory, String name) {
        return (directory.equals("/") ? "" : directory) + "/" + name;
    }

    public static String parentOf(String path) {
        if (path.equals("/")) throw new IllegalArgumentException("无法获取根的父目录");
        int idx = path.lastIndexOf('/');
        return idx == 0 ? "/" : path.substring(0, idx);
    }

    public static String nameOf(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
