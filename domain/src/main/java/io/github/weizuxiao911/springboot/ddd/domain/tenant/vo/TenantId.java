package io.github.weizuxiao911.springboot.ddd.domain.tenant.vo;

import io.github.weizuxiao911.springboot.ddd.common.id.ID;
import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;

/**
 * 租户ID值对象
 * 封装租户ID的创建、校验和转换逻辑。
 *
 * @param value ID值
 */
public record TenantId(String value) {

    /**
     * 紧凑构造函数
     * 校验ID值不能为空。
     */
    public TenantId {
        if (value == null || value.isBlank()) {
            throw DomainException.invalidState("TenantId cannot be empty");
        }
    }

    /**
     * 从字符串创建TenantId
     *
     * @param value 字符串值
     * @return TenantId实例
     */
    public static TenantId of(String value) {
        return new TenantId(value);
    }

    /**
     * 生成新的TenantId（使用雪花算法）
     *
     * @return TenantId实例
     */
    public static TenantId generate() {
        return new TenantId(String.valueOf(ID.getInstance().generate()));
    }

    @Override
    public String toString() {
        return value;
    }
}