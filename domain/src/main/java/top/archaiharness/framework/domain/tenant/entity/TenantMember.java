package top.archaiharness.framework.domain.tenant.entity;

import top.archaiharness.framework.common.base.AggregateRoot;
import top.archaiharness.framework.common.exception.DomainException;
import top.archaiharness.framework.domain.tenant.vo.TenantId;
import top.archaiharness.framework.domain.tenant.vo.TenantMemberId;
import top.archaiharness.framework.domain.tenant.vo.TenantRole;
import top.archaiharness.framework.domain.user.vo.UserId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TenantMember 聚合根
 * 表示租户与用户的关联关系，具有独立身份标识，可通过Repository管理。
 * 支持大规模成员场景，可分页查询。
 * 
 * 注意：TenantMember 是独立的聚合根（而非 Tenant 聚合内的实体），
 * 这是为了支持大规模成员场景（一个租户可能有数万成员）而做出的架构决策。
 * Tenant 与 TenantMember 之间通过值对象（TenantId, UserId）进行引用，而非直接聚合关系。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class TenantMember extends AggregateRoot {

    /** 成员唯一标识 */
    @EqualsAndHashCode.Include
    private TenantMemberId id;

    /** 租户ID */
    private TenantId tenantId;

    /** 用户ID */
    private UserId userId;

    /** 角色 */
    private TenantRole role;

    /**
     * 工厂方法：创建租户成员（向后兼容）
     *
     * @param tenantId 租户ID
     * @param userId  用户ID
     * @param role   角色
     * @return TenantMember实例
     */
    public static TenantMember create(TenantId tenantId, UserId userId, TenantRole role) {
        return new Builder()
            .tenantId(tenantId)
            .userId(userId)
            .role(role)
            .build();
    }

    /**
     * 自定义 Builder 类
     * 强制校验必填字段，自动生成 ID
     */
    public static class Builder {
        private TenantId tenantId;
        private UserId userId;
        private TenantRole role;

        public Builder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder userId(UserId userId) {
            this.userId = userId;
            return this;
        }

        public Builder role(TenantRole role) {
            this.role = role;
            return this;
        }

        public TenantMember build() {
            if (tenantId == null) {
                throw DomainException.invalidState("TenantId cannot be null");
            }
            if (userId == null) {
                throw DomainException.invalidState("UserId cannot be null");
            }
            if (role == null) {
                throw DomainException.invalidState("Role cannot be null");
            }

            TenantMember member = new TenantMember();
            member.id = TenantMemberId.generate();
            member.tenantId = this.tenantId;
            member.userId = this.userId;
            member.role = this.role;
            return member;
        }
    }

    /**
     * 升级为管理员
     * 前置条件：当前角色不是 OWNER。
     *
     * @throws DomainException 当前是所有者时抛出
     */
    public void promoteToAdmin() {
        if (this.role == TenantRole.OWNER) {
            throw DomainException.invalidState("Cannot change owner role");
        }
        this.role = TenantRole.ADMIN;
    }

    /**
     * 降级为普通成员
     * 前置条件：当前角色不是 OWNER。
     *
     * @throws DomainException 当前是所有者时抛出
     */
    public void demoteToMember() {
        if (this.role == TenantRole.OWNER) {
            throw DomainException.invalidState("Cannot change owner role");
        }
        this.role = TenantRole.MEMBER;
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