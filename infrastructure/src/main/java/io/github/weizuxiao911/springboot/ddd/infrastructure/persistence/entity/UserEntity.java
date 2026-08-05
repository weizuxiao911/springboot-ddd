package io.github.weizuxiao911.springboot.ddd.infrastructure.persistence.entity;

import io.github.weizuxiao911.springboot.ddd.domain.user.entity.User;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserStatus;
import io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户 JPA 实体
 * 对应数据库表 t_user，内部封装与 Domain Entity 的转换逻辑
 */
@Entity
@Table(name = "t_user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    /** 用户唯一标识（业务 ID） */
    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    /** 用户名（登录凭证） */
    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    /** 邮箱地址 */
    @Column(name = "email", length = 128)
    private String email;

    /** 手机号码 */
    @Column(name = "phone", length = 32)
    private String phone;

    /** 用户状态 */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * JPA Entity → Domain Entity
     * 使用 reconstruct() 方法重建用户，避免重新生成 ID 和注册虚假事件
     */
    public User toDomain() {
        return User.reconstruct(
                UserId.of(userId),
                username,
                email,
                phone,
                UserStatus.valueOf(status)
        );
    }

    /**
     * Domain Entity → JPA Entity
     */
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getId().value());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setStatus(user.getStatus().getValue());
        return entity;
    }
}
