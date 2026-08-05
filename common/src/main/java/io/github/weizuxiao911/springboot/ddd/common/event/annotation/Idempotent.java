package io.github.weizuxiao911.springboot.ddd.common.event.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记事件处理方法需要幂等保护。
 * 由 infrastructure 层 AOP 切面自动拦截，检查/记录 eventId。
 * 
 * <p>默认开启，所有 @OnEvent 方法自动应用此注解效果。
 * 如需关闭，设置 enabled = false（需审批）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 是否启用幂等保护
     */
    boolean enabled() default true;
}
