package io.github.weizuxiao911.springboot.ddd.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldSetAndGetUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "user-123");
        user.put("name", "John Doe");

        UserContext.setUser(user);

        Object retrievedUser = UserContext.getUser();
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) retrievedUser;
        assertThat(userMap.get("id")).isEqualTo("user-123");
        assertThat(userMap.get("name")).isEqualTo("John Doe");
    }

    @Test
    void shouldSetAndGetAttribute() {
        UserContext.setAttribute("custom-key", "custom-value");

        assertThat(UserContext.getAttribute("custom-key")).isEqualTo("custom-value");
    }

    @Test
    void shouldReturnNullForMissingAttribute() {
        assertThat(UserContext.getAttribute("missing-key")).isNull();
    }

    @Test
    void shouldReturnEmptyMapWhenNoContext() {
        Map<String, Object> context = UserContext.getContext();

        assertThat(context).isEmpty();
    }

    @Test
    void shouldGetCompleteContext() {
        UserContext.setAttribute("key1", "value1");
        UserContext.setAttribute("key2", "value2");

        Map<String, Object> context = UserContext.getContext();

        assertThat(context).hasSize(2);
        assertThat(context.get("key1")).isEqualTo("value1");
        assertThat(context.get("key2")).isEqualTo("value2");
    }

    @Test
    void shouldClearContext() {
        UserContext.setAttribute("key", "value");
        UserContext.clear();

        assertThat(UserContext.getAttribute("key")).isNull();
        assertThat(UserContext.getContext()).isEmpty();
    }

    @Test
    void shouldCheckAuthentication() {
        assertThat(UserContext.isAuthenticated()).isFalse();

        Map<String, Object> user = new HashMap<>();
        user.put("id", "user-123");
        UserContext.setUser(user);

        assertThat(UserContext.isAuthenticated()).isTrue();

        UserContext.clear();
        assertThat(UserContext.isAuthenticated()).isFalse();
    }

    @Test
    void shouldMaintainThreadIsolation() throws InterruptedException {
        final String[] thread1Result = new String[1];
        final String[] thread2Result = new String[1];

        Thread thread1 = new Thread(() -> {
            UserContext.setAttribute("test-key", "thread1-value");
            thread1Result[0] = (String) UserContext.getAttribute("test-key");
        });

        Thread thread2 = new Thread(() -> {
            UserContext.setAttribute("test-key", "thread2-value");
            thread2Result[0] = (String) UserContext.getAttribute("test-key");
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertThat(thread1Result[0]).isEqualTo("thread1-value");
        assertThat(thread2Result[0]).isEqualTo("thread2-value");
    }
}