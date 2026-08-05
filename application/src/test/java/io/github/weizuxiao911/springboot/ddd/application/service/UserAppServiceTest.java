package io.github.weizuxiao911.springboot.ddd.application.service;

import io.github.weizuxiao911.springboot.ddd.application.dto.command.CreateUserCommand;
import io.github.weizuxiao911.springboot.ddd.application.dto.query.GetUserQuery;
import io.github.weizuxiao911.springboot.ddd.application.dto.query.GetUserInfoFromRemoteQuery;
import io.github.weizuxiao911.springboot.ddd.application.dto.response.UserResponse;
import io.github.weizuxiao911.springboot.ddd.common.event.DomainEventPublisher;
import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.entity.User;
import io.github.weizuxiao911.springboot.ddd.domain.user.repository.UserRepository;
import io.github.weizuxiao911.springboot.ddd.domain.user.service.UserDomainService;
import io.github.weizuxiao911.springboot.ddd.domain.user.service.UserService;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private UserService userService;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private UserAppService userAppService;

    private CreateUserCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateUserCommand();
        command.setUsername("zhangsan");
        command.setEmail("zhangsan@example.com");
        command.setTenantId("tenant-001");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userDomainService.isUsernameUnique("zhangsan", null)).thenReturn(true);
        when(userDomainService.isEmailUnique("zhangsan@example.com", null)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userAppService.createUser(command);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("zhangsan");
        assertThat(response.getEmail()).isEqualTo("zhangsan@example.com");
        assertThat(response.getStatus()).isEqualTo("active");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameNotUnique() {
        when(userDomainService.isUsernameUnique("zhangsan", null)).thenReturn(false);

        assertThatThrownBy(() -> userAppService.createUser(command))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "USERNAME_TAKEN");
    }

    @Test
    void shouldThrowExceptionWhenEmailNotUnique() {
        when(userDomainService.isUsernameUnique("zhangsan", null)).thenReturn(true);
        when(userDomainService.isEmailUnique("zhangsan@example.com", null)).thenReturn(false);

        assertThatThrownBy(() -> userAppService.createUser(command))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_TAKEN");
    }

    @Test
    void shouldGetUserSuccessfully() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        UserId userId = user.getId();
        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        GetUserQuery query = new GetUserQuery();
        query.setUserId(userId.value());

        UserResponse response = userAppService.getUser(query);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    void shouldThrowDomainExceptionWhenUserNotFound() {
        String nonExistentUserId = "non-existent-id";
        UserId userId = UserId.of(nonExistentUserId);
        when(userDomainService.getUserOrThrow(userId)).thenThrow(DomainException.notFound("User", nonExistentUserId));

        GetUserQuery query = new GetUserQuery();
        query.setUserId(nonExistentUserId);

        assertThatThrownBy(() -> userAppService.getUser(query))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldGetUserInfoFromRemoteSuccessfully() {
        String userIdStr = "123";
        UserId userId = UserId.of(userIdStr);
        UserInfo userInfo = new UserInfo(userId, "zhangsan", "zhangsan@example.com", "tenant-001");
        when(userService.getUserInfo(userId)).thenReturn(userInfo);
        GetUserInfoFromRemoteQuery query = new GetUserInfoFromRemoteQuery();
        query.setUserId(userIdStr);

        UserResponse response = userAppService.getUserInfoFromRemote(query);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("zhangsan");
        assertThat(response.getEmail()).isEqualTo("zhangsan@example.com");
        verify(userService).getUserInfo(userId);
    }

    @Test
    void shouldUpdateEmailSuccessfully() {
        User user = User.create("zhangsan", "old@example.com");
        UserId userId = user.getId();
        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userDomainService.isEmailUnique("new@example.com", userId)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userAppService.updateEmail(userId.value(), "new@example.com");

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailToDuplicate() {
        User user = User.create("zhangsan", "old@example.com");
        UserId userId = user.getId();
        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userDomainService.isEmailUnique("duplicate@example.com", userId)).thenReturn(false);

        assertThatThrownBy(() -> userAppService.updateEmail(userId.value(), "duplicate@example.com"))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_TAKEN");
    }

    @Test
    void shouldDeactivateUserSuccessfully() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        UserId userId = user.getId();
        when(userDomainService.getUserOrThrow(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userAppService.deactivate(userId.value());

        assertThat(response.getStatus()).isEqualTo("deactivated");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        String nonExistentUserId = "non-existent-id";
        UserId userId = UserId.of(nonExistentUserId);
        when(userDomainService.getUserOrThrow(userId)).thenThrow(DomainException.notFound("User", nonExistentUserId));

        assertThatThrownBy(() -> userAppService.deactivate(nonExistentUserId))
                .isInstanceOf(DomainException.class);
    }
}