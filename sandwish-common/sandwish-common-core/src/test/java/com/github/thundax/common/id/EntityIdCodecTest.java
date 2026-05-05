package com.github.thundax.common.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class EntityIdCodecTest {

    @Test
    public void shouldConvertStringToEntityId() {
        EntityId entityId = EntityIdCodec.toDomain("user-1");

        assertEquals(EntityId.of("user-1"), entityId);
    }

    @Test
    public void shouldConvertEntityIdToString() {
        assertEquals("user-1", EntityIdCodec.toValue(EntityId.of("user-1")));
        assertNull(EntityIdCodec.toValue(null));
    }

    @Test
    public void shouldConvertStringListToEntityIdList() {
        List<EntityId> ids = EntityIdCodec.toDomains(Arrays.asList("user-1", null, " "));

        assertEquals(EntityId.of("user-1"), ids.get(0));
        assertNull(ids.get(1));
        assertNull(ids.get(2));
    }

    @Test
    public void shouldConvertEntityIdListToStringList() {
        List<String> values = EntityIdCodec.toValues(Arrays.asList(EntityId.of("user-1"), null));

        assertEquals("user-1", values.get(0));
        assertNull(values.get(1));
    }

    @Test
    public void shouldKeepNullListAsNull() {
        assertNull(EntityIdCodec.toDomains(null));
        assertNull(EntityIdCodec.toValues(null));
    }
}
