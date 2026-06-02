package top.archaiharness.framework.domain.tenant.vo;

/**
 * 租户角色枚举
 * 定义用户在租户内的角色层级。
 */
public enum TenantRole {
    /** 租户所有者，拥有最高权限 */
    OWNER,
    /** 租户管理员，拥有管理权限 */
    ADMIN,
    /** 租户普通成员 */
    MEMBER
}