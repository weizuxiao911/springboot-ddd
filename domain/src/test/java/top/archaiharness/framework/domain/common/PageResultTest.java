package top.archaiharness.framework.domain.common;

import top.archaiharness.framework.common.pagination.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void shouldCreatePageResultWithCorrectTotalPages() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 10, 1, 3);

        assertThat(result.getItems()).containsExactly("a", "b");
        assertThat(result.getTotal()).isEqualTo(10);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(4);
    }

    @Test
    void shouldReturnTrueWhenHasNext() {
        PageResult<String> result = new PageResult<>(List.of("a"), 10, 1, 3);

        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnLastPage() {
        PageResult<String> result = new PageResult<>(List.of("a", "b"), 10, 4, 3);

        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenHasPrevious() {
        PageResult<String> result = new PageResult<>(List.of("a"), 10, 2, 3);

        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnFirstPage() {
        PageResult<String> result = new PageResult<>(List.of("a"), 10, 1, 3);

        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    void shouldCreateEmptyPageResult() {
        PageResult<String> result = PageResult.empty(1, 10);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isZero();
    }
}
