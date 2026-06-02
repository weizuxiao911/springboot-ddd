package top.archaiharness.framework.interfaces.config.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import top.archaiharness.framework.common.context.AppContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 应用上下文过滤器
 * 从 HTTP 请求中提取所有 Header 并存入 AppContext
 * 自动将 TraceId 注入 MDC 以支持日志链路追踪
 * 请求结束时自动清理上下文，防止内存泄漏
 */
@Component
@Order(1)
public class AppContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    headers.put(name, httpRequest.getHeader(name));
                }
            }
            AppContext.setHeaders(headers);

            String traceId = AppContext.getHeader(AppContext.Keys.TRACE_ID);
            if (traceId != null) {
                MDC.put("traceId", traceId);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            AppContext.clear();
        }
    }
}
