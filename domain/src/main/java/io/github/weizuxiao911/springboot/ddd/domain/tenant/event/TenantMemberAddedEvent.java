package io.github.weizuxiao911.springboot.ddd.domain.tenant.event;

import io.github.weizuxiao911.springboot.ddd.common.event.DomainEvent;
import lombok.Getter;

/**
 * 租户成员添加事件
 * 成员添加到租户后发布。
 */
@Getter
public class TenantMemberAddedEvent extends DomainEvent {

    /** 租户ID */
    private final String tenantId;

    /** 用户ID */
    private final String userId;

    /** 角色 */
    private final String role;

    /**
     * 构造函数
     *
     * @param tenantId 租户ID
     * @param userId  用户ID
     * @param role    角色
     */
    public TenantMemberAddedEvent(String tenantId, String userId, String role) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.role = role;
    }
}