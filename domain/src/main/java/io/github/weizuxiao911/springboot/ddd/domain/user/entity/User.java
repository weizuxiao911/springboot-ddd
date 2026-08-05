package io.github.weizuxiao911.springboot.ddd.domain.user.entity;

import io.github.weizuxiao911.springboot.ddd.common.base.AggregateRoot;
import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.event.UserCreatedEvent;
import io.github.weizuxiao911.springboot.ddd.domain.user.event.UserUpdatedEvent;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserStatus;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserTenant;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantId;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User 聚合根
 * 负责管理用户身份、状态及生命周期，维护用户业务不变式。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends AggregateRoot {

    /** 用户唯一标识 */
    @EqualsAndHashCode.Include
    private UserId id;

    /** 用户名（登录凭证） */
    private String username;

    /** 邮箱地址 */
    private String email;

    /** 手机号码 */
    private String phone;

    /** 用户状态（ACTIVE/DEACTIVATED/LOCKED） */
    private UserStatus status;

    /** 用户关联的租户列表 */
    private List<UserTenant> tenants = new ArrayList<>();

    /**
     * 专用构造函数：从数据库重建用户（供 Infrastructure 层使用）
     * 不生成事件，不校验参数，直接设置所有字段。
     *
     * @param id      用户ID
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @param status   用户状态
     */
    private User(UserId id, String username, String email, String phone, UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    /**
     * 工厂方法：创建新用户（向后兼容）
     * 自动生成 ID，设置默认状态为 ACTIVE，并注册 UserCreatedEvent。
     *
     * @param username 用户名
     * @param email    邮箱
     * @return 新创建的 User 实例
     * @throws DomainException 参数为空时抛出
     */
    public static User create(String username, String email) {
        return new Builder()
            .username(username)
            .email(email)
            .build();
    }

    /**
     * 自定义 Builder 类
     * 强制校验必填字段，自动生成 ID，注册领域事件
     */
    public static class Builder {
        private String username;
        private String email;
        private String phone;
        private UserStatus status;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public User build() {
            if (username == null || username.isBlank()) {
                throw DomainException.invalidState("Username cannot be empty");
            }
            if (email == null || email.isBlank()) {
                throw DomainException.invalidState("Email cannot be empty");
            }

            User user = new User();
            user.id = UserId.generate();
            user.username = this.username;
            user.email = this.email;
            user.phone = this.phone;
            user.status = this.status != null ? this.status : UserStatus.ACTIVE;

            user.registerEvent(new UserCreatedEvent(user.id.value(), this.username));
            return user;
        }
    }

    /**
     * 重建方法：从数据库重建用户（供 Infrastructure 层使用）
     * 不生成事件，直接设置所有字段。
     *
     * @param id      用户ID
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @param status   用户状态
     * @return 重建的 User 实例
     */
    public static User reconstruct(UserId id, String username, String email, String phone, UserStatus status) {
        return new User(id, username, email, phone, status);
    }

    /**
     * 更新邮箱
     * 前置条件：邮箱不能为空。
     * 副作用：注册 UserUpdatedEvent。
     */
    public void updateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw DomainException.invalidState("Email cannot be empty");
        }
        this.email = email;
        this.registerEvent(new UserUpdatedEvent(this.id.value(), "email"));
    }

    /**
     * 更新手机号码
     * 副作用：注册 UserUpdatedEvent。
     */
    public void updatePhone(String phone) {
        this.phone = phone;
        this.registerEvent(new UserUpdatedEvent(this.id.value(), "phone"));
    }

    /**
     * 停用用户
     * 前置条件：用户当前未处于 DEACTIVATED 状态。
     * 副作用：状态变更为 DEACTIVATED，注册 UserUpdatedEvent。
     */
    public void deactivate() {
        if (this.status == UserStatus.DEACTIVATED) {
            throw DomainException.invalidState("User is already deactivated");
        }
        this.status = UserStatus.DEACTIVATED;
        this.registerEvent(new UserUpdatedEvent(this.id.value(), "status"));
    }

    /**
     * 激活用户
     * 前置条件：用户当前未处于 ACTIVE 状态。
     * 副作用：状态变更为 ACTIVE，注册 UserUpdatedEvent。
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw DomainException.invalidState("User is already active");
        }
        this.status = UserStatus.ACTIVE;
        this.registerEvent(new UserUpdatedEvent(this.id.value(), "status"));
    }

    /**
     * 检查用户是否处于活跃状态
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public void addTenant(TenantId tenantId, TenantRole role) {
        if (tenantId == null) {
            throw DomainException.invalidState("TenantId cannot be null");
        }
        if (hasTenant(tenantId)) {
            throw DomainException.invalidState("User is already a member of this tenant");
        }
        this.tenants.add(new UserTenant(tenantId, role));
    }

    public void removeTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw DomainException.invalidState("TenantId cannot be null");
        }
        if (!hasTenant(tenantId)) {
            throw DomainException.invalidState("User is not a member of this tenant");
        }
        this.tenants.removeIf(t -> t.tenantId().equals(tenantId));
    }

    public boolean hasTenant(TenantId tenantId) {
        return tenants.stream().anyMatch(t -> t.tenantId().equals(tenantId));
    }

    public boolean hasAdminPrivileges(TenantId tenantId) {
        return tenants.stream()
                .filter(t -> t.tenantId().equals(tenantId))
                .anyMatch(UserTenant::hasAdminPrivileges);
    }

    public List<UserTenant> getTenants() {
        return Collections.unmodifiableList(tenants);
    }
}
