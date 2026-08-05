package io.github.weizuxiao911.springboot.ddd.application.service;

import io.github.weizuxiao911.springboot.ddd.application.dto.command.CreateUserCommand;
import io.github.weizuxiao911.springboot.ddd.application.dto.query.GetUserQuery;
import io.github.weizuxiao911.springboot.ddd.application.dto.query.GetUserInfoFromRemoteQuery;
import io.github.weizuxiao911.springboot.ddd.application.dto.response.UserResponse;
import io.github.weizuxiao911.springboot.ddd.common.annotation.Cache;
import io.github.weizuxiao911.springboot.ddd.common.event.DomainEventPublisher;
import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.entity.User;
import io.github.weizuxiao911.springboot.ddd.domain.user.repository.UserRepository;
import io.github.weizuxiao911.springboot.ddd.domain.user.service.UserDomainService;
import io.github.weizuxiao911.springboot.ddd.domain.user.service.UserService;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务
 * 负责用户相关的用例编排、事务管理、事件发布。
 */
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final UserService userService;
    private final DomainEventPublisher eventPublisher;

    /**
     * 创建用户
     * 1. 校验用户名唯一性
     * 2. 校验邮箱唯一性
     * 3. 创建用户聚合
     * 4. 保存并发布事件
     */
    @Transactional
    public UserResponse createUser(CreateUserCommand command) {
        if (!userDomainService.isUsernameUnique(command.getUsername(), null)) {
            throw DomainException.of("USERNAME_TAKEN", "用户名已被占用");
        }
        if (!userDomainService.isEmailUnique(command.getEmail(), null)) {
            throw DomainException.of("EMAIL_TAKEN", "邮箱已被占用");
        }

        User user = User.create(
            command.getUsername(),
            command.getEmail()
        );

        userRepository.save(user);

        eventPublisher.publish(user.getDomainEvents());
        user.clearDomainEvents();

        return toResponse(user);
    }

    /**
     * 获取用户
     */
    @Transactional(readOnly = true)
    @Cache(key = "#query.getUserId()")
    public UserResponse getUser(GetUserQuery query) {
        UserId id = UserId.of(query.getUserId());
        User user = userDomainService.getUserOrThrow(id);
        return toResponse(user);
    }

    /**
     * 跨服务交互示例：通过 Domain 层外部服务接口获取其他服务数据
     * 注意：注入的是 Domain 层接口，不是 Feign 客户端
     */
    @Transactional(readOnly = true)
    public UserResponse getUserInfoFromRemote(GetUserInfoFromRemoteQuery query) {
        UserId userId = UserId.of(query.getUserId());
        UserInfo userInfo = userService.getUserInfo(userId);
        return toResponseFromUserInfo(userInfo);
    }

    @Transactional
    public UserResponse updateEmail(String userId, String email) {
        UserId id = UserId.of(userId);
        User user = userDomainService.getUserOrThrow(id);

        if (!userDomainService.isEmailUnique(email, id)) {
            throw DomainException.of("EMAIL_TAKEN", "邮箱已被占用");
        }

        user.updateEmail(email);
        userRepository.save(user);

        eventPublisher.publish(user.getDomainEvents());
        user.clearDomainEvents();

        return toResponse(user);
    }

    @Transactional
    public UserResponse deactivate(String userId) {
        UserId id = UserId.of(userId);
        User user = userDomainService.getUserOrThrow(id);

        user.deactivate();
        userRepository.save(user);

        eventPublisher.publish(user.getDomainEvents());
        user.clearDomainEvents();

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId().value());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus().getValue());
        return response;
    }

    private UserResponse toResponseFromUserInfo(UserInfo userInfo) {
        UserResponse response = new UserResponse();
        response.setUserId(userInfo.userId().value());
        response.setUsername(userInfo.username());
        response.setEmail(userInfo.email());
        return response;
    }
}