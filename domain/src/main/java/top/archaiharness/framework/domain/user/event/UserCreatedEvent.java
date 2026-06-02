package top.archaiharness.framework.domain.user.event;

import top.archaiharness.framework.common.event.DomainEvent;
import lombok.Getter;

/**
 * 用户创建事件
 * 用户创建成功后发布。
 */
@Getter
public class UserCreatedEvent extends DomainEvent {

    /** 用户ID */
    private final String userId;

    /** 用户名 */
    private final String username;

    /**
     * 构造函数
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public UserCreatedEvent(String userId, String username) {
        super();
        this.userId = userId;
        this.username = username;
    }
}
