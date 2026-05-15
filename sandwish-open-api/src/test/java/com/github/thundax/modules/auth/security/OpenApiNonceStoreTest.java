package com.github.thundax.modules.auth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OpenApiNonceStoreTest {

    @Test
    public void shouldRejectReplayedNonceInWindow() {
        OpenApiNonceStore store = new OpenApiNonceStore(1000L);

        assertTrue(store.markUsed("swak_demo", "nonce-1", 1000L));
        assertFalse(store.markUsed("swak_demo", "nonce-1", 1100L));
        assertTrue(store.markUsed("swak_demo", "nonce-2", 1100L));
    }

    @Test
    public void shouldAllowSameNonceAfterTtlExpired() {
        OpenApiNonceStore store = new OpenApiNonceStore(1000L);

        assertTrue(store.markUsed("swak_demo", "nonce-1", 1000L));
        assertTrue(store.markUsed("swak_demo", "nonce-1", 2101L));
    }
}
