package top.archaiharness.framework.infrastructure.persistence.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

import top.archaiharness.framework.infrastructure.config.jpa.repository.BaseJpaRepository;
import top.archaiharness.framework.infrastructure.persistence.entity.UserEntity;

/**
 * 用户 JPA 仓库
 * 提供基于 UserEntity 的数据访问能力，由 UserRepositoryImpl 调用
 */
@Repository
public interface UserJpaRepository extends BaseJpaRepository<UserEntity> {

    Optional<UserEntity> findByUserId(String userId);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
