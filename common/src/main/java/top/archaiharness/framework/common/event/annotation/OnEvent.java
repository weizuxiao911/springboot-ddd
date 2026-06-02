package top.archaiharness.framework.common.event.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记事件处理方法，由基础设施层自动扫描并注册
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnEvent {
    /**
     * 事件类型
     */
    Class<? extends top.archaiharness.framework.common.event.DomainEvent> value();
}
