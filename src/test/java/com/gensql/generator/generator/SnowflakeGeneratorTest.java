package com.gensql.generator.generator;

import com.gensql.generator.model.dto.FieldConfig;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeGeneratorTest {

    @Test
    void shouldGenerateUniqueIdsInSingleInstance() {
        SnowflakeGenerator generator = new SnowflakeGenerator(1, 1, 5);
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            ids.add(generator.generate(new FieldConfig()));
        }

        assertTrue(ids.size() == 1000);
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentWorkerIds() {
        SnowflakeGenerator generator1 = new SnowflakeGenerator(1, 1, 5);
        SnowflakeGenerator generator2 = new SnowflakeGenerator(2, 1, 5);

        Long id1 = generator1.generate(new FieldConfig());
        Long id2 = generator2.generate(new FieldConfig());

        assertNotEquals(id1, id2);
    }

    @Test
    void shouldRejectInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeGenerator(32, 1, 5));
    }

    @Test
    void shouldRejectInvalidDatacenterId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeGenerator(1, 32, 5));
    }

    @Test
    void shouldRejectLargeClockBackwardOffset() {
        SnowflakeGenerator generator = new BackwardClockSnowflakeGenerator(1, 1, 5, 1000L, 990L);
        generator.generate(new FieldConfig());

        assertThrows(IllegalStateException.class, () -> generator.generate(new FieldConfig()));
    }

    private static class BackwardClockSnowflakeGenerator extends SnowflakeGenerator {
        private final long[] timestamps;
        private int index = 0;

        BackwardClockSnowflakeGenerator(long workerId, long datacenterId, long maxBackwardMillis, long... timestamps) {
            super(workerId, datacenterId, maxBackwardMillis);
            this.timestamps = timestamps;
        }

        @Override
        protected long timeGen() {
            if (index >= timestamps.length) {
                return timestamps[timestamps.length - 1];
            }
            return timestamps[index++];
        }
    }
}
