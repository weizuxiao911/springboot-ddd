package top.archaiharness.framework.interfaces.config.interceptor;

import top.archaiharness.framework.common.context.AppContext;
import top.archaiharness.framework.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // Gateway已经完成JWT验证，这里只需传递上下文
        // 可以根据需要添加业务权限验证逻辑
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        AppContext.clear();
        UserContext.clear();
    }
}
