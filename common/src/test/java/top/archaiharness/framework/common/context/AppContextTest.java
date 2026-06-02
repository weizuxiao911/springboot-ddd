package top.archaiharness.framework.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextTest {

    @AfterEach
    void tearDown() {
        AppContext.clear();
    }

    @Test
    void shouldSetAndGetHeader() {
        AppContext.setHeader("X-User-Id", "user-123");

        assertThat(AppContext.getHeader("X-User-Id")).isEqualTo("user-123");
    }

    @Test
    void shouldReturnNullForMissingHeader() {
        assertThat(AppContext.getHeader("Missing")).isNull();
    }

    @Test
    void shouldSetAndGetAllHeaders() {
        AppContext.setHeader("X-User-Id", "user-123");
        AppContext.setHeader("X-Tenant-Id", "tenant-456");

        Map<String, String> headers = AppContext.getHeaders();

        assertThat(headers).hasSize(2);
        assertThat(headers.get("X-User-Id")).isEqualTo("user-123");
        assertThat(headers.get("X-Tenant-Id")).isEqualTo("tenant-456");
    }

    @Test
    void shouldReturnEmptyMapWhenNoHeaders() {
        Map<String, String> headers = AppContext.getHeaders();

        assertThat(headers).isEmpty();
    }

    @Test
    void shouldSetHeadersInBulk() {
        Map<String, String> headers = Map.of(
                "X-User-Id", "user-123",
                "X-Tenant-Id", "tenant-456"
        );
        AppContext.setHeaders(headers);

        assertThat(AppContext.getHeader("X-User-Id")).isEqualTo("user-123");
        assertThat(AppContext.getHeader("X-Tenant-Id")).isEqualTo("tenant-456");
    }

    @Test
    void shouldClearContext() {
        AppContext.setHeader("X-User-Id", "user-123");
        AppContext.clear();

        assertThat(AppContext.getHeader("X-User-Id")).isNull();
    }

    @Test
    void shouldProvideCommonKeys() {
        assertThat(AppContext.Keys.USER_ID).isEqualTo("X-User-Id");
        assertThat(AppContext.Keys.TENANT_ID).isEqualTo("X-Tenant-Id");
        assertThat(AppContext.Keys.TRACE_ID).isEqualTo("X-Trace-Id");
    }
}
