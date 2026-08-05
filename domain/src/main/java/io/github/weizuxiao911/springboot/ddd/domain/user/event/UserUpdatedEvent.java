package io.github.weizuxiao911.springboot.ddd.domain.user.event;

import io.github.weizuxiao911.springboot.ddd.common.event.DomainEvent;
import lombok.Getter;

/**
 * 用户更新事件
 * 用户信息更新后发布。
 */
@Getter
public class UserUpdatedEvent extends DomainEvent {

    /** 用户ID */
    private final String userId;

    /** 更新的字段名 */
    private final String field;

    /**
     * 构造函数
     *
     * @param userId 用户ID
     * @param field 更新的字段名
     */
    public UserUpdatedEvent(String userId, String field) {
        super();
        this.userId = userId;
        this.field = field;
    }
}
