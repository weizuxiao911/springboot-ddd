package io.github.weizuxiao911.springboot.ddd.common.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 *
 * 使用示例：
 * <pre>
 * {@code
 * @RequirePermission(
 *     resource = "USER",
 *     resourceValue = "#userId",
 *     action = "READ"
 * )
 * public User getUserById(Long userId) {
 *     return userRepository.findById(userId);
 * }
 * }
 * </pre>
 *
 * @author framework
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 资源类型（如：USER, ORDER, PRODUCT等）
     *
     * @return 资源类型
     */
    String resource();

    /**
     * 资源标识
     * 支持SpEL表达式，例如：
     * - "#userId" - 方法参数
     * - "#result.id" - 返回值属性
     * - "'/api/users/*'" - 固定值
     *
     * @return 资源标识
     */
    String resourceValue();

    /**
     * 动作（如：READ, WRITE, DELETE等）
     *
     * @return 动作
     */
    String action();

    /**
     * 条件（可选）
     * 支持SpEL表达式，例如：
     * - "T(LocalDateTime).now().isBefore(T(LocalTime).of(18,0))" - 时间条件
     * - "#userContext.tenantId == 1" - 租户条件
     *
     * @return 条件表达式
     */
    String condition() default "";
}
