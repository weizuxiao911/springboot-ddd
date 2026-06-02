package top.archaiharness.framework.domain.user.vo;

import top.archaiharness.framework.common.exception.DomainException;
import lombok.Getter;

/**
 * 用户状态枚举
 * 定义用户的所有可能状态。
 */
@Getter
public enum UserStatus {

    /** 活跃状态 */
    ACTIVE("active"),

    /** 已停用状态 */
    DEACTIVATED("deactivated"),

    /** 已锁定状态 */
    LOCKED("locked");

    /** 状态值 */
    private final String value;

    /**
     * 构造函数
     *
     * @param value 状态值
     */
    UserStatus(String value) {
        this.value = value;
    }

    /**
     * 根据值查找状态
     *
     * @param value 状态值
     * @return 对应的状态
     * @throws DomainException 未知状态值
     */
    public static UserStatus fromValue(String value) {
        for (UserStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw DomainException.invalidState("Unknown UserStatus: " + value);
    }
}
