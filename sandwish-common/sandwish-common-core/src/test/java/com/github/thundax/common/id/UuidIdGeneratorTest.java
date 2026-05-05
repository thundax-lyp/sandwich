package com.github.thundax.common.id;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UuidIdGeneratorTest {

    @Test
    public void shouldGenerateNonBlankDistinctId() {
        UuidIdGenerator generator = new UuidIdGenerator();

        EntityId first = generator.nextId();
        EntityId second = generator.nextId();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }
}
