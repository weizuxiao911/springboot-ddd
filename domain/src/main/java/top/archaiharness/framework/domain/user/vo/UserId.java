package top.archaiharness.framework.domain.user.vo;

import top.archaiharness.framework.common.exception.DomainException;
import top.archaiharness.framework.common.id.ID;

/**
 * 用户ID值对象
 * 封装用户ID的创建、校验和转换逻辑。
 *
 * @param value ID值
 */
public record UserId(String value) {

    /**
     * 紧凑构造函数
     * 校验ID值不能为空。
     */
    public UserId {
        if (value == null || value.isBlank()) {
            throw DomainException.invalidState("UserId cannot be empty");
        }
    }

    /**
     * 从字符串创建UserId
     *
     * @param value 字符串值
     * @return UserId实例
     */
    public static UserId of(String value) {
        return new UserId(value);
    }

    /**
     * 生成新的UserId（使用UUID）
     *
     * @return UserId实例
     */
    public static UserId generate() {
        return new UserId(String.valueOf(ID.getInstance().generate()));
    }

    @Override
    public String toString() {
        return value;
    }
}
