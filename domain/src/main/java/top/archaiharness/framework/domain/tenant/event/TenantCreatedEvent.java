package top.archaiharness.framework.domain.tenant.event;

import top.archaiharness.framework.common.event.DomainEvent;
import lombok.Getter;

/**
 * 租户创建事件
 * 租户创建成功后发布。
 */
@Getter
public class TenantCreatedEvent extends DomainEvent {

    /** 租户ID */
    private final String tenantId;

    /** 租户名称 */
    private final String tenantName;

    /** 所有者用户ID */
    private final String ownerUserId;

    /**
     * 构造函数
     *
     * @param tenantId    租户ID
     * @param tenantName 租户名称
     * @param ownerUserId 所有者用户ID
     */
    public TenantCreatedEvent(String tenantId, String tenantName, String ownerUserId) {
        super();
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.ownerUserId = ownerUserId;
    }
}