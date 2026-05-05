package com.github.thundax.common.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EntityIdTest {

    @Test
    public void shouldCreateEntityIdFromNonBlankValue() {
        EntityId id = EntityId.of("user-1");

        assertEquals("user-1", id.value());
        assertEquals("user-1", id.toString());
        assertEquals(String.class, id.type());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectBlankValue() {
        EntityId.of(" ");
    }

    @Test
    public void shouldReturnNullForNullableBlankValue() {
        assertNull(EntityId.ofNullable(null));
        assertNull(EntityId.ofNullable(""));
        assertNull(EntityId.ofNullable(" "));
    }

    @Test
    public void shouldCompareByTypeAndValue() {
        assertEquals(EntityId.of("id-1"), EntityId.of("id-1"));
        assertNotEquals(EntityId.of("id-1"), EntityId.of("id-2"));
    }
}
