package io.github.weizuxiao911.springboot.ddd.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解
 * 用于在 Application Service 方法上声明分布式锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    /**
     * 锁的 Key，支持 SpEL 表达式 (如 "#userId")
     */
    String key();

    /**
     * 获取锁的最大等待时间 (秒)
     */
    long waitTime() default 3;

    /**
     * 锁自动释放时间 (秒)
     */
    long leaseTime() default 10;
}
