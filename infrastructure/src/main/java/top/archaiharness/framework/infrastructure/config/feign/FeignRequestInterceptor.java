package top.archaiharness.framework.infrastructure.config.feign;

import java.util.Enumeration;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import top.archaiharness.framework.common.context.AppContext;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 请求拦截器
 * 优先使用 AppContext 透传请求头（支持异步线程）
 * 降级使用 RequestContextHolder（兼容非异步场景）
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Map<String, String> headers = AppContext.getHeaders();

        // 1. 优先使用 AppContext 中的 Header (支持 @Async 异步透传)
        if (!headers.isEmpty()) {
            propagateHeaders(headers, template);
            return;
        }

        // 2. 降级使用 Spring 原生 RequestContextHolder (同步场景)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    template.header(name, request.getHeader(name));
                }
            }
        }
    }

    /**
     * 透传 x- 开头的请求头
     */
    private void propagateHeaders(Map<String, String> headers, RequestTemplate template) {
        headers.forEach((name, value) -> {
            if (name.toLowerCase().startsWith("x-")) {
                log.debug("透传请求头：{} = {}", name, value);
                template.header(name, value);
            }
        });
    }
}
