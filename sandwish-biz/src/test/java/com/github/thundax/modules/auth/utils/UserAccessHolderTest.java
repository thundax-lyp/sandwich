package com.github.thundax.modules.auth.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

public class UserAccessHolderTest {

    @After
    public void tearDown() {
        UserAccessHolder.clear();
    }

    @Test
    public void shouldSetAndClearCurrentUserAccess() {
        UserAccessHolder.currentUserId("user-1", "token-1");

        assertEquals("user-1", UserAccessHolder.currentUserId());
        assertEquals("token-1", UserAccessHolder.currentToken());

        UserAccessHolder.clear();

        assertNull(UserAccessHolder.currentUserId());
        assertNull(UserAccessHolder.currentToken());
    }

    @Test
    public void shouldClearWhenCurrentUserIdIsNull() {
        UserAccessHolder.currentUserId("user-1", "token-1");

        UserAccessHolder.currentUserId(null, "token-2");

        assertNull(UserAccessHolder.currentUserId());
        assertNull(UserAccessHolder.currentToken());
    }
}
