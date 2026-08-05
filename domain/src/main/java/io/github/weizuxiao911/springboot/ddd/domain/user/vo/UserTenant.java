package io.github.weizuxiao911.springboot.ddd.domain.user.vo;

import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantId;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole;
import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;

/**
 * 用户租户关联值对象
 * 表示用户与租户的关联关系及角色。
 *
 * @param tenantId 租户ID
 * @param role    角色
 */
public record UserTenant(TenantId tenantId, TenantRole role) {

    /**
     * 紧凑构造函数
     * 校验tenantId和role不能为空。
     */
    public UserTenant {
        if (tenantId == null) {
            throw DomainException.invalidState("TenantId cannot be null");
        }
        if (role == null) {
            throw DomainException.invalidState("Role cannot be null");
        }
    }

    /**
     * 从字符串创建UserTenant
     *
     * @param tenantId 租户ID字符串
     * @param role    角色字符串
     * @return UserTenant实例
     */
    public static UserTenant of(String tenantId, String role) {
        return new UserTenant(TenantId.of(tenantId), TenantRole.valueOf(role));
    }

    /**
     * 判断是否为所有者
     *
     * @return 是否为所有者
     */
    public boolean isOwner() {
        return role == TenantRole.OWNER;
    }

    /**
     * 判断是否具有管理权限
     *
     * @return 是否具有管理权限
     */
    public boolean hasAdminPrivileges() {
        return role == TenantRole.OWNER || role == TenantRole.ADMIN;
    }
}