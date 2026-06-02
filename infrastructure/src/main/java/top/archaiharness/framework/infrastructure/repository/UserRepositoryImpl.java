package top.archaiharness.framework.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import top.archaiharness.framework.domain.user.entity.User;
import top.archaiharness.framework.domain.user.repository.UserRepository;
import top.archaiharness.framework.domain.user.vo.UserId;
import top.archaiharness.framework.infrastructure.persistence.entity.UserEntity;
import top.archaiharness.framework.infrastructure.persistence.repository.UserJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 用户仓库实现
 * 实现 domain 层定义的 UserRepository 接口，转换逻辑委托给 UserEntity
 */
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @SuppressWarnings("null")
    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        jpaRepository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findByUserId(id.value()).map(UserEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(UserEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.findByUserId(id.value()).ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.findByUserId(id.value()).isPresent();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
