package io.github.weizuxiao911.springboot.ddd.domain.user.service;

import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.entity.User;
import io.github.weizuxiao911.springboot.ddd.domain.user.repository.UserRepository;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;

import java.util.Optional;

/**
 * 用户领域服务接口
 * 负责跨聚合协调逻辑和复杂业务规则校验。
 */
public interface UserDomainService {

    /**
     * 获取用户仓储
     */
    UserRepository getUserRepository();

    /**
     * 校验用户名唯一性
     *
     * @param username 用户名
     * @param excludeUserId 排除的用户ID（用于更新场景）
     * @return 是否唯一
     */
    default boolean isUsernameUnique(String username, UserId excludeUserId) {
        Optional<User> existingUser = getUserRepository().findByUsername(username);
        if (existingUser.isEmpty()) {
            return true;
        }
        return existingUser.get().getId().equals(excludeUserId);
    }

    /**
     * 校验邮箱唯一性
     *
     * @param email 邮箱
     * @param excludeUserId 排除的用户ID（用于更新场景）
     * @return 是否唯一
     */
    default boolean isEmailUnique(String email, UserId excludeUserId) {
        Optional<User> existingUser = getUserRepository().findByEmail(email);
        if (existingUser.isEmpty()) {
            return true;
        }
        return existingUser.get().getId().equals(excludeUserId);
    }

    /**
     * 校验用户是否存在
     *
     * @param userId 用户ID
     * @throws DomainException 用户不存在
     */
    default void validateUserExists(UserId userId) {
        getUserRepository().findById(userId)
                .orElseThrow(() -> DomainException.notFound("User", userId.toString()));
    }

    /**
     * 获取用户或抛出异常
     *
     * @param userId 用户ID
     * @return 用户实体
     * @throws DomainException 用户不存在
     */
    default User getUserOrThrow(UserId userId) {
        return getUserRepository().findById(userId)
                .orElseThrow(() -> DomainException.notFound("User", userId.toString()));
    }
}
