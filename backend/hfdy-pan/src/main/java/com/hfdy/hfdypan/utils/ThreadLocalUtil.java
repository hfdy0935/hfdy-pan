package com.hfdy.hfdypan.utils;

/**
 * @author hf-dy
 * @date 2024/10/22 11:20
 */

public class ThreadLocalUtil {

    private static final ThreadLocal<String> thread = new ThreadLocal<>();

    public static String getCurrentUserId() {
        return thread.get();
    }

    public static void setCurrentUserId(String userId) {
        thread.set(userId);
    }

    public static void delete() {
        thread.remove();
    }
}
