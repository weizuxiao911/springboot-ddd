package io.github.weizuxiao911.springboot.ddd.domain.tenant.repository;

import io.github.weizuxiao911.springboot.ddd.common.base.BaseRepository;
import io.github.weizuxiao911.springboot.ddd.common.pagination.PageQuery;
import io.github.weizuxiao911.springboot.ddd.common.pagination.PageResult;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.entity.TenantMember;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantId;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantMemberId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 租户成员仓库接口
 * 定义租户成员持久化能力。
 */
public interface TenantMemberRepository extends BaseRepository<TenantMember, TenantMemberId> {

    /**
     * 根据租户ID查询成员列表
     *
     * @param tenantId 租户ID
     * @return 成员列表
     */
    List<TenantMember> findByTenantId(TenantId tenantId);

    /**
     * 根据用户ID查询成员列表（用户可能属于多个租户）
     *
     * @param userId 用户ID
     * @return 成员列表
     */
    List<TenantMember> findByUserId(UserId userId);

    /**
     * 分页查询租户成员
     *
     * @param tenantId   租户ID
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<TenantMember> findByTenantId(TenantId tenantId, PageQuery pageQuery);

    /**
     * 根据租户ID和用户ID查询成员
     *
     * @param tenantId 租户ID
     * @param userId  用户ID
     * @return 成员 Optional
     */
    Optional<TenantMember> findByTenantIdAndUserId(TenantId tenantId, UserId userId);

    /**
     * 判断用户是否是租户成员
     *
     * @param tenantId 租户ID
     * @param userId  用户ID
     * @return 是否是成员
     */
    boolean existsByTenantIdAndUserId(TenantId tenantId, UserId userId);

    /**
     * 根据租户ID删除成员
     *
     * @param tenantId 租户ID
     */
    void deleteByTenantId(TenantId tenantId);
}