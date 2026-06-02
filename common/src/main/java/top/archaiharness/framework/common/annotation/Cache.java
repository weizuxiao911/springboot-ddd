package top.archaiharness.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存注解
 * 用于在 Application Service 方法上声明缓存策略
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    /**
     * 缓存的 Key，支持 SpEL 表达式 (如 "#userId")
     */
    String key();

    /**
     * 缓存过期时间 (秒)
     */
    long expire() default 300;
}
