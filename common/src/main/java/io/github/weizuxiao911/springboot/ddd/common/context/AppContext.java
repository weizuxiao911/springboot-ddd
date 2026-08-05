package io.github.weizuxiao911.springboot.ddd.common.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用上下文持有者
 * 用于在当前线程及异步子线程中透传请求头信息
 * 纯 Java 实现，无框架依赖，可被 interfaces 和 infrastructure 共享使用
 */
public class AppContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT = new ThreadLocal<>();

    private AppContext() {
    }

    /**
     * 获取指定请求头
     */
    public static String getHeader(String name) {
        Map<String, String> headers = CONTEXT.get();
        return headers != null ? headers.get(name) : null;
    }

    /**
     * 获取所有请求头（只读视图）
     */
    public static Map<String, String> getHeaders() {
        Map<String, String> headers = CONTEXT.get();
        return headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
    }

    /**
     * 设置请求头（用于 Web 过滤器或测试）
     */
    public static void setHeader(String name, String value) {
        Map<String, String> headers = CONTEXT.get();
        if (headers == null) {
            headers = new HashMap<>();
            CONTEXT.set(headers);
        }
        headers.put(name, value);
    }

    /**
     * 批量设置请求头
     */
    public static void setHeaders(Map<String, String> headers) {
        Map<String, String> current = CONTEXT.get();
        if (current == null) {
            CONTEXT.set(new HashMap<>(headers));
        } else {
            current.putAll(headers);
        }
    }

    /**
     * 清理上下文，防止内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 常用 Key 定义
     */
    public static class Keys {
        public static final String TRACE_ID = "X-Trace-Id";
        public static final String TENANT_ID = "X-Tenant-Id";
        public static final String USER_ID = "X-User-Id";
    }
}
