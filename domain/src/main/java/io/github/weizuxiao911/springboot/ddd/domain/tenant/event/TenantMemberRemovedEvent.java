package io.github.weizuxiao911.springboot.ddd.domain.tenant.event;

import io.github.weizuxiao911.springboot.ddd.common.event.DomainEvent;
import lombok.Getter;

/**
 * 租户成员移除事件
 * 成员从租户移除后发布。
 */
@Getter
public class TenantMemberRemovedEvent extends DomainEvent {

    /** 租户ID */
    private final String tenantId;

    /** 用户ID */
    private final String userId;

    /**
     * 构造函数
     *
     * @param tenantId 租户ID
     * @param userId  用户ID
     */
    public TenantMemberRemovedEvent(String tenantId, String userId) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
    }
}