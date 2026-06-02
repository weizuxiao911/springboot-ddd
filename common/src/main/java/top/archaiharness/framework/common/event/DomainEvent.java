package top.archaiharness.framework.common.event;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 所有领域事件必须继承此类。
 */
@Data
public abstract class DomainEvent {

    /** 事件唯一标识（用于消费者幂等去重） */
    private String eventId;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 事件类型 */
    private String eventType;

    /**
     * 默认构造函数
     * 自动设置事件ID、发生时间和事件类型。
     */
    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }
}
