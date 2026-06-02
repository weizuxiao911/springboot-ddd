package top.archaiharness.framework.common.id;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IDTest {

    @Test
    void shouldGenerateUniqueIds() {
        ID snowId = ID.getInstance();

        Long id1 = snowId.generate();
        Long id2 = snowId.generate();

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldGenerateIncreasingIds() {
        ID snowId = ID.getInstance();

        Long id1 = snowId.generate();
        Long id2 = snowId.generate();

        assertThat(id2).isGreaterThan(id1);
    }

    @Test
    void shouldThrowExceptionWhenDatacenterIdIsInvalid() {
        assertThatThrownBy(() -> new ID(-1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenMachineIdIsInvalid() {
        assertThatThrownBy(() -> new ID(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldGenerateShortIds() {
        String shortId = ID.getInstance().generate(8);

        assertThat(shortId).hasSize(8);
        assertThat(shortId).matches("[0-9a-zA-Z]+");
    }

    @Test
    void shouldGenerateUniqueShortIdsUnderConcurrency() {
        int count = 10000;
        Set<String> ids = ConcurrentHashMap.newKeySet();

        IntStream.range(0, count).parallel().forEach(i -> {
            ids.add(ID.getInstance().generate(8));
        });

        assertThat(ids).hasSize(count);
    }
}
