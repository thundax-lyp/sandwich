package com.github.thundax.common.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EntityIdTest {

    @Test
    public void shouldCreateEntityIdFromPositiveLongValue() {
        EntityId id = EntityId.of(1001L);

        assertEquals(Long.valueOf(1001L), id.value());
        assertEquals("1001", id.toString());
        assertEquals(Long.class, id.type());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectZeroValue() {
        EntityId.of(0L);
    }

    @Test
    public void shouldReturnNullForNullableValue() {
        assertNull(EntityId.ofNullable((Long) null));
    }

    @Test
    public void shouldCompareByTypeAndValue() {
        assertEquals(EntityId.of(1001L), EntityId.of(1001L));
        assertNotEquals(EntityId.of(1001L), EntityId.of(1002L));
    }
}
