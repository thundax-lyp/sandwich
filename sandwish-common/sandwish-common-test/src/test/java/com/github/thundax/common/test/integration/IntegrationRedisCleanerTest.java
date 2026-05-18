package com.github.thundax.common.test.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class IntegrationRedisCleanerTest {

    @Test
    public void shouldDeleteKeysByPrefix() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        Set<byte[]> keys = new LinkedHashSet<byte[]>();
        keys.add(bytes("sandwish:it:a"));
        keys.add(bytes("sandwish:it:b"));
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.keys(bytes("sandwish:it:*"))).thenReturn(keys);
        when(connection.del(keys.toArray(new byte[keys.size()][]))).thenReturn(2L);

        long deleted = new IntegrationRedisCleaner(connectionFactory).cleanByPrefix("sandwish:it:");

        assertEquals(2L, deleted);
        verify(connection).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectBlankPrefix() {
        new IntegrationRedisCleaner(mock(RedisConnectionFactory.class)).cleanByPrefix("");
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
