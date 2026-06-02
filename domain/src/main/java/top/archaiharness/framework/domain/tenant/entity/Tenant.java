package top.archaiharness.framework.domain.tenant.entity;

import top.archaiharness.framework.common.base.AggregateRoot;
import top.archaiharness.framework.common.exception.DomainException;
import top.archaiharness.framework.domain.tenant.event.TenantCreatedEvent;
import top.archaiharness.framework.domain.tenant.vo.TenantId;
import top.archaiharness.framework.domain.user.vo.UserId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Tenant 聚合根
 * 负责管理租户信息、成员及生命周期，维护租户业务不变式。
 * 成员通过 TenantMember 实体管理，支持大规模成员场景。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Tenant extends AggregateRoot {

    /** 租户唯一标识 */
    @EqualsAndHashCode.Include
    private TenantId id;

    /** 租户名称 */
    private String name;

    /** 所有者用户ID（用于创建时注册事件） */
    private UserId ownerUserId;

    /**
     * 工厂方法：创建新租户（向后兼容）
     * 自动生成 ID，创建者自动成为所有者（OWNER），并注册 TenantCreatedEvent。
     *
     * @param name        租户名称
     * @param ownerUserId 所有者用户ID
     * @return 新创建的 Tenant 实例
     * @throws DomainException 名称为空时抛出
     */
    public static Tenant create(String name, UserId ownerUserId) {
        return new Builder()
            .name(name)
            .ownerUserId(ownerUserId)
            .build();
    }

    /**
     * 自定义 Builder 类
     * 强制校验必填字段，自动生成 ID，注册领域事件
     */
    public static class Builder {
        private String name;
        private UserId ownerUserId;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ownerUserId(UserId ownerUserId) {
            this.ownerUserId = ownerUserId;
            return this;
        }

        public Tenant build() {
            if (name == null || name.isBlank()) {
                throw DomainException.invalidState("Tenant name cannot be empty");
            }
            if (ownerUserId == null) {
                throw DomainException.invalidState("Owner user cannot be null");
            }

            Tenant tenant = new Tenant();
            tenant.id = TenantId.generate();
            tenant.name = this.name;
            tenant.ownerUserId = this.ownerUserId;
            tenant.registerEvent(new TenantCreatedEvent(tenant.id.value(), tenant.name, this.ownerUserId.toString()));
            return tenant;
        }
    }

    /**
     * 更新租户名称
     * 前置条件：名称不能为空。
     *
     * @param name 租户名称
     */
    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw DomainException.invalidState("Tenant name cannot be empty");
        }
        this.name = name;
    }

    /**
     * 检查用户是否是成员（基于加载的成员列表）
     * 注意：需要配合领域服务加载成员列表使用。
     *
     * @param userId     用户ID
     * @param members   已加载的成员列表
     * @return 是否是成员
     */
    public boolean hasMember(UserId userId, List<TenantMember> members) {
        return members.stream().anyMatch(m -> m.getUserId().equals(userId));
    }

    /**
     * 检查用户是否是所有者
     *
     * @param userId  用户ID
     * @param members 已加载的成员列表
     * @return 是否是所有者
     */
    public boolean isOwner(UserId userId, List<TenantMember> members) {
        return members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .anyMatch(TenantMember::isOwner);
    }

    /**
     * 检查用户是否具有管理权限
     *
     * @param userId  用户ID
     * @param members 已加载的成员列表
     * @return 是否具有管理权限
     */
    public boolean hasAdminPrivileges(UserId userId, List<TenantMember> members) {
        return members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .anyMatch(TenantMember::hasAdminPrivileges);
    }
}