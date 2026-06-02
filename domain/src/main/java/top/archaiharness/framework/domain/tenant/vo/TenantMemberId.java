package top.archaiharness.framework.domain.tenant.vo;

import top.archaiharness.framework.common.id.ID;
import top.archaiharness.framework.common.exception.DomainException;

/**
 * 租户成员ID值对象
 * 封装租户成员ID的创建、校验和转换逻辑。
 *
 * @param value ID值
 */
public record TenantMemberId(String value) {

    /**
     * 紧凑构造函数
     * 校验ID值不能为空。
     */
    public TenantMemberId {
        if (value == null || value.isBlank()) {
            throw DomainException.invalidState("TenantMemberId cannot be empty");
        }
    }

    /**
     * 从字符串创建TenantMemberId
     *
     * @param value 字符串值
     * @return TenantMemberId实例
     */
    public static TenantMemberId of(String value) {
        return new TenantMemberId(value);
    }

    /**
     * 生成新的TenantMemberId（使用雪花算法）
     *
     * @return TenantMemberId实例
     */
    public static TenantMemberId generate() {
        return new TenantMemberId(String.valueOf(ID.getInstance().generate()));
    }

    @Override
    public String toString() {
        return value;
    }
}