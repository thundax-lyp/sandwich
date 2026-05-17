package com.github.thundax.modules.auth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class OpenApiNonceStoreTest {

    @Test
    public void shouldRejectReplayedNonceInWindow() {
        TestNonceWriter nonceWriter = new TestNonceWriter();
        OpenApiNonceStore store = new OpenApiNonceStore(1000L, nonceWriter);

        nonceWriter.now(1000L);
        assertTrue(store.markUsed("swak_demo", "nonce-1"));
        nonceWriter.now(1100L);
        assertFalse(store.markUsed("swak_demo", "nonce-1"));
        assertTrue(store.markUsed("swak_demo", "nonce-2"));
    }

    @Test
    public void shouldAllowSameNonceAfterTtlExpired() {
        TestNonceWriter nonceWriter = new TestNonceWriter();
        OpenApiNonceStore store = new OpenApiNonceStore(1000L, nonceWriter);

        nonceWriter.now(1000L);
        assertTrue(store.markUsed("swak_demo", "nonce-1"));
        nonceWriter.now(2101L);
        assertTrue(store.markUsed("swak_demo", "nonce-1"));
    }

    @Test
    public void shouldRejectReplayedNonceAcrossInstancesSharingRemoteCache() {
        TestNonceWriter nonceWriter = new TestNonceWriter();
        OpenApiNonceStore firstInstance = new OpenApiNonceStore(1000L, nonceWriter);
        OpenApiNonceStore secondInstance = new OpenApiNonceStore(1000L, nonceWriter);

        nonceWriter.now(1000L);
        assertTrue(firstInstance.markUsed("swak_demo", "nonce-1"));
        nonceWriter.now(1100L);
        assertFalse(secondInstance.markUsed("swak_demo", "nonce-1"));
    }

    private static class TestNonceWriter implements OpenApiNonceStore.NonceWriter {
        private final Map<String, Entry> values = new HashMap<>();
        private long now;

        private void now(long now) {
            this.now = now;
        }

        @Override
        public boolean setIfAbsent(String key, String value, long ttlMillis) {
            Entry existing = values.get(key);
            if (existing != null && existing.expiresAt > now) {
                return false;
            }
            values.put(key, new Entry(value, now + ttlMillis));
            return true;
        }
    }

    private static class Entry {
        private final String value;
        private final long expiresAt;

        private Entry(String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
