package io.github.weizuxiao911.springboot.ddd.common.pagination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageQueryTest {

    @Test
    void shouldHaveDefaultValues() {
        PageQuery query = new PageQuery();

        assertThat(query.getPage()).isEqualTo(1);
        assertThat(query.getSize()).isEqualTo(20);
        assertThat(query.getSortDirection()).isEqualTo("DESC");
    }

    @Test
    void shouldCalculateOffset() {
        PageQuery query = new PageQuery();
        query.setPage(3);
        query.setSize(10);

        assertThat(query.getOffset()).isEqualTo(20);
    }

    @Test
    void shouldValidatePageNumber() {
        PageQuery query = new PageQuery();
        query.setPage(0);
        query.validate();

        assertThat(query.getPage()).isEqualTo(1);
    }

    @Test
    void shouldValidatePageSize() {
        PageQuery query = new PageQuery();
        query.setSize(200);
        query.validate();

        assertThat(query.getSize()).isEqualTo(20);
    }
}
