package io.github.weizuxiao911.springboot.ddd.common.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户上下文
 * 存储当前请求的用户信息，支持动态属性以适配 PBAC 策略。
 * 
 * 设计原则：
 * 1. 只负责上下文存储，不包含业务逻辑
 * 2. 保持简单，避免过度设计
 * 3. 提供通用的属性存取方法
 */
public class UserContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    private UserContext() {}

    /**
     * 设置当前用户信息
     * 通常在JWT认证拦截器中调用
     */
    public static void setUser(Map<String, Object> user) {
        setAttribute("user", user);
    }

    /**
     * 获取当前用户对象（用于 SpEL 表达式求值）
     */
    public static Object getUser() {
        return getAttribute("user");
    }

    /**
     * 设置通用属性
     */
    public static void setAttribute(String key, Object value) {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            map = new HashMap<>();
            CONTEXT.set(map);
        }
        map.put(key, value);
    }

    /**
     * 获取通用属性
     */
    public static Object getAttribute(String key) {
        Map<String, Object> map = CONTEXT.get();
        return map != null ? map.get(key) : null;
    }

    /**
     * 获取完整的用户上下文信息
     */
    public static Map<String, Object> getContext() {
        Map<String, Object> map = CONTEXT.get();
        return map != null ? map : Map.of();
    }

    /**
     * 清除当前线程的用户上下文
     * 通常在请求完成后调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户 ID
     */
    public static String getCurrentUserId() {
        return AppContext.getHeader(AppContext.Keys.USER_ID);
    }

    /**
     * 获取当前租户 ID
     */
    public static String getCurrentTenantId() {
        return AppContext.getHeader(AppContext.Keys.TENANT_ID);
    }

    /**
     * 获取当前 Trace ID
     */
    public static String getTraceId() {
        return AppContext.getHeader(AppContext.Keys.TRACE_ID);
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isAuthenticated() {
        return getUser() != null;
    }
}