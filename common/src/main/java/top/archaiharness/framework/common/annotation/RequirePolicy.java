package top.archaiharness.framework.common.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 *
 * 使用示例：
 * <pre>
 * {@code
 * // 固定权限码
 * @RequirePolicy(permissionCode = "USER:DELETE")
 * public void deleteUser(Long userId) {
 *     userRepository.deleteById(userId);
 * }
 *
 * // SpEL表达式
 * @RequirePolicy(permissionCode = "#resourceType + ':VIEW'")
 * public Resource getResource(@PathVariable String resourceType) {
 *     return resourceService.findByType(resourceType);
 * }
 *
 * // 跨租户权限检查
 * @RequirePolicy(permissionCode = "#tenantId + ':USER:VIEW'", tenantId = "#tenantId")
 * public User getUserByTenant(@PathVariable Long tenantId, @PathVariable Long userId) {
 *     return userService.findByTenantAndId(tenantId, userId);
 * }
 * }
 * </pre>
 *
 * @author framework
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePolicy {

    /**
     * 权限码
     * 支持SpEL表达式，可从方法参数、UserContext、Request中动态计算
     *
     * @return 权限码表达式
     */
    String permissionCode();

    /**
     * 租户ID（可选）
     * 如果不指定，默认检查当前租户的权限
     * 支持SpEL表达式
     *
     * @return 租户ID表达式
     */
    String tenantId() default "";
}
