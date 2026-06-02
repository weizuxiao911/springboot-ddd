package top.archaiharness.framework.common.event;

import java.util.List;

/**
 * 领域事件发布器接口
 * 定义领域事件发布的能力，由基础设施层实现。
 */
public interface DomainEventPublisher {

    /**
     * 发布领域事件
     *
     * @param events 要发布的事件列表
     */
    void publish(List<DomainEvent> events);
}
