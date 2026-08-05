package io.github.weizuxiao911.springboot.ddd.common.pbac.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class AccessContextTest {

    @Test
    void shouldCreateEmptyAccessContext() {
        AccessContext context = new AccessContext();

        assertThat(context.getUserContext()).isNull();
        assertThat(context.getVariables()).isEmpty();
    }

    @Test
    void shouldCreateAccessContextWithUserContext() {
        UserPermissionContext userContext = new UserPermissionContext(
                1L, 100L, null, null);

        AccessContext context = new AccessContext(userContext);

        assertThat(context.getUserContext()).isEqualTo(userContext);
        assertThat(context.getVariables()).isEmpty();
    }

    @Test
    void shouldSetUserContext() {
        AccessContext context = new AccessContext();
        UserPermissionContext userContext = new UserPermissionContext(
                1L, 100L, null, null);

        context.setUserContext(userContext);

        assertThat(context.getUserContext()).isEqualTo(userContext);
    }

    @Test
    void shouldPutAndGetVariable() {
        AccessContext context = new AccessContext();

        context.putVariable("userId", 123L);
        context.putVariable("tenantId", 456L);

        assertThat(context.get("userId")).isEqualTo(123L);
        assertThat(context.get("tenantId")).isEqualTo(456L);
    }

    @Test
    void shouldReturnNullForMissingVariable() {
        AccessContext context = new AccessContext();

        assertThat(context.get("nonexistent")).isNull();
    }

    @Test
    void shouldPutAllVariables() {
        AccessContext context = new AccessContext();
        Map<String, Object> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");

        context.putAllVariables(variables);

        assertThat(context.getVariables()).hasSize(2);
        assertThat(context.get("key1")).isEqualTo("value1");
        assertThat(context.get("key2")).isEqualTo("value2");
    }

    @Test
    void shouldHandleNullVariablesWhenPutAll() {
        AccessContext context = new AccessContext();

        context.putAllVariables(null);

        assertThat(context.getVariables()).isEmpty();
    }

    @Test
    void shouldMergeVariablesWhenPutAll() {
        AccessContext context = new AccessContext();
        context.putVariable("existing", "value");

        Map<String, Object> newVariables = new HashMap<>();
        newVariables.put("new-key", "new-value");

        context.putAllVariables(newVariables);

        assertThat(context.getVariables()).hasSize(2);
        assertThat(context.get("existing")).isEqualTo("value");
        assertThat(context.get("new-key")).isEqualTo("new-value");
    }
}