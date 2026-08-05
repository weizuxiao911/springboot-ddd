package io.github.weizuxiao911.springboot.ddd.domain.common;

import io.github.weizuxiao911.springboot.ddd.common.pagination.PageQuery;
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
    void shouldCalculateCorrectOffset() {
        PageQuery query = new PageQuery();
        query.setPage(3);
        query.setSize(10);

        assertThat(query.getOffset()).isEqualTo(20);
    }

    @Test
    void shouldValidateAndCorrectPageWhenLessThanOne() {
        PageQuery query = new PageQuery();
        query.setPage(0);
        query.setSize(10);

        query.validate();

        assertThat(query.getPage()).isEqualTo(1);
    }

    @Test
    void shouldValidateAndCorrectSizeWhenLessThanOne() {
        PageQuery query = new PageQuery();
        query.setPage(1);
        query.setSize(0);

        query.validate();

        assertThat(query.getSize()).isEqualTo(20);
    }

    @Test
    void shouldValidateAndCorrectSizeWhenGreaterThan100() {
        PageQuery query = new PageQuery();
        query.setPage(1);
        query.setSize(200);

        query.validate();

        assertThat(query.getSize()).isEqualTo(20);
    }

    @Test
    void shouldKeepValidValuesAfterValidate() {
        PageQuery query = new PageQuery();
        query.setPage(5);
        query.setSize(50);

        query.validate();

        assertThat(query.getPage()).isEqualTo(5);
        assertThat(query.getSize()).isEqualTo(50);
    }
}
