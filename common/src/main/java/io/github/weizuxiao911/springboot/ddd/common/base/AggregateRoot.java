package io.github.weizuxiao911.springboot.ddd.common.base;

import io.github.weizuxiao911.springboot.ddd.common.event.DomainEvent;
import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 所有聚合根必须继承此类，以获得领域事件注册能力。
 */
@Getter
public abstract class AggregateRoot {

    /** 领域事件列表 */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 注册领域事件
     *
     * @param event 要注册的事件
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * 获取领域事件列表（只读）
     *
     * @return 不可变的领域事件列表
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清空领域事件列表
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
