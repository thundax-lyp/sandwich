package com.github.thundax.common.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SnowflakeIdGeneratorTest {

    @Test
    public void shouldGenerateNonBlankDistinctId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1L);

        EntityId first = generator.nextId();
        EntityId second = generator.nextId();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidWorkerId() {
        new SnowflakeIdGenerator(1024L);
    }

    @Test
    public void shouldIncreaseSequenceWithinSameMillis() {
        SnowflakeIdGenerator generator = new FixedTimeSnowflakeIdGenerator(1L, 1700000000000L);

        EntityId first = generator.nextId();
        EntityId second = generator.nextId();

        assertEquals(Long.valueOf(first.value() + 1L), second.value());
    }

    private static class FixedTimeSnowflakeIdGenerator extends SnowflakeIdGenerator {

        private final long timestamp;

        FixedTimeSnowflakeIdGenerator(long workerId, long timestamp) {
            super(workerId);
            this.timestamp = timestamp;
        }

        @Override
        protected long currentTimeMillis() {
            return timestamp;
        }
    }
}
