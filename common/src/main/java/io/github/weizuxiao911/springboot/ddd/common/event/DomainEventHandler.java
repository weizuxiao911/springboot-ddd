package io.github.weizuxiao911.springboot.ddd.common.event;

/**
 * 事件处理接口，由应用层实现
 */
public interface DomainEventHandler<T extends DomainEvent> {
    
    /**
     * 处理事件
     */
    void handle(T event);
    
    /**
     * 返回支持的事件类型
     */
    Class<T> eventType();
}
