package io.github.weizuxiao911.springboot.ddd.domain.user.service;

import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.entity.User;
import io.github.weizuxiao911.springboot.ddd.domain.user.repository.UserRepository;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserDomainService userDomainService;

    @BeforeEach
    void setUp() {
        userDomainService = new UserDomainService() {
            @Override
            public UserRepository getUserRepository() {
                return userRepository;
            }
        };
    }

    @Test
    void shouldReturnTrueWhenUsernameIsUnique() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        boolean result = userDomainService.isUsernameUnique("newuser", null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenUsernameBelongsToSameUser() {
        User existingUser = User.create("testuser", "test@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        boolean result = userDomainService.isUsernameUnique("testuser", existingUser.getId());

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameBelongsToAnotherUser() {
        User existingUser = User.create("testuser", "test@example.com");
        UserId anotherUserId = UserId.generate();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        boolean result = userDomainService.isUsernameUnique("testuser", anotherUserId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailIsUnique() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        boolean result = userDomainService.isEmailUnique("new@example.com", null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenEmailBelongsToSameUser() {
        User existingUser = User.create("testuser", "test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        boolean result = userDomainService.isEmailUnique("test@example.com", existingUser.getId());

        assertThat(result).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInValidateUserExists() {
        UserId userId = UserId.generate();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDomainService.validateUserExists(userId))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "NOT_FOUND");
    }

    @Test
    void shouldNotThrowExceptionWhenUserExistsInValidateUserExists() {
        UserId userId = UserId.generate();
        User user = User.create("testuser", "test@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userDomainService.validateUserExists(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInGetUserOrThrow() {
        UserId userId = UserId.generate();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDomainService.getUserOrThrow(userId))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "NOT_FOUND");
    }

    @Test
    void shouldReturnUserWhenUserExistsInGetUserOrThrow() {
        User user = User.create("testuser", "test@example.com");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userDomainService.getUserOrThrow(user.getId());

        assertThat(result).isEqualTo(user);
    }
}
