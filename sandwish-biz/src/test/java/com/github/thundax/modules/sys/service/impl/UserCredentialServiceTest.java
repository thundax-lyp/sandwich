package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import java.util.Date;
import org.junit.Test;

public class UserCredentialServiceTest {

    @Test
    public void shouldMarkCredentialFailedAndLockedWhenLimitReached() {
        Date lockedUntil = new Date(System.currentTimeMillis() + 60000L);
        UserCredential credential = new UserCredential();
        credential.setStatus(UserCredentialStatus.ACTIVE);
        credential.setFailedCount(2);
        credential.setFailedLimit(3);

        credential.markFailed(lockedUntil);

        assertSame(UserCredentialStatus.LOCKED, credential.getStatus());
        assertTrue(credential.isLocked(new Date()));
        assertSame(lockedUntil, credential.getLockedUntil());
    }

    @Test
    public void shouldKeepCredentialActiveBeforeFailureLimit() {
        UserCredential credential = new UserCredential();
        credential.setStatus(UserCredentialStatus.ACTIVE);
        credential.setFailedCount(0);
        credential.setFailedLimit(3);

        credential.markFailed(new Date(System.currentTimeMillis() + 60000L));

        assertSame(UserCredentialStatus.ACTIVE, credential.getStatus());
        assertFalse(credential.isLocked(new Date()));
    }

    @Test
    public void shouldClearFailureStateAfterVerified() {
        Date verifiedAt = new Date();
        UserCredential credential = new UserCredential();
        credential.setStatus(UserCredentialStatus.LOCKED);
        credential.setFailedCount(3);
        credential.setFailedLimit(3);
        credential.setLockedUntil(new Date(System.currentTimeMillis() + 60000L));

        credential.markVerified(verifiedAt);

        assertSame(UserCredentialStatus.ACTIVE, credential.getStatus());
        assertFalse(credential.isLocked(new Date()));
        assertSame(verifiedAt, credential.getLastVerifiedAt());
    }
}
