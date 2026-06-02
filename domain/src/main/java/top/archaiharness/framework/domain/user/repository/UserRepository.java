package top.archaiharness.framework.domain.user.repository;

import top.archaiharness.framework.common.base.BaseRepository;
import top.archaiharness.framework.domain.user.entity.User;
import top.archaiharness.framework.domain.user.vo.UserId;

import java.util.Optional;

/**
 * 用户仓库接口
 * 定义用户聚合的持久化能力，由基础设施层实现。
 */
public interface UserRepository extends BaseRepository<User, UserId> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户Optional
     */
    Optional<User> findByPhone(String phone);

    /**
     * 判断用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
