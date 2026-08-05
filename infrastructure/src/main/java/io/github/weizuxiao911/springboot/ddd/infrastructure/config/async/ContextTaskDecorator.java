package io.github.weizuxiao911.springboot.ddd.infrastructure.config.async;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import io.github.weizuxiao911.springboot.ddd.common.context.AppContext;

/**
 * 线程池上下文透传装饰器
 * 用于在 @Async 等异步场景下，将父线程的 AppContext 和 MDC 传递给子线程
 * 解决线程池复用导致的上下文丢失或脏数据问题
 */
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public @NonNull Runnable decorate(@NonNull Runnable runnable) {
        // 1. 捕获父线程上下文
        Map<String, String> parentContext = AppContext.getHeaders();
        Map<String, String> parentMdcContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 2. 设置 AppContext 到子线程（覆盖可能存在的脏数据）
                parentContext.forEach(AppContext::setHeader);

                // 3. 设置 MDC 到子线程
                if (parentMdcContext != null) {
                    MDC.setContextMap(parentMdcContext);
                }

                // 4. 执行任务
                runnable.run();
            } finally {
                // 5. 清理子线程上下文，防止内存泄漏和污染下一次任务
                AppContext.clear();
                MDC.clear();
            }
        };
    }
}
