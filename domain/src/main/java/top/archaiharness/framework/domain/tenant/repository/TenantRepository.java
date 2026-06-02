package top.archaiharness.framework.domain.tenant.repository;

import top.archaiharness.framework.common.base.BaseRepository;
import top.archaiharness.framework.domain.tenant.entity.Tenant;
import top.archaiharness.framework.domain.tenant.vo.TenantId;

import java.util.Optional;

/**
 * 租户仓库接口
 * 定义租户持久化能力。
 */
public interface TenantRepository extends BaseRepository<Tenant, TenantId> {

    /**
     * 根据租户名称查询
     *
     * @param name 租户名称
     * @return 租户 Optional
     */
    Optional<Tenant> findByName(String name);

    /**
     * 判断租户名称是否存在
     *
     * @param name 租户名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}