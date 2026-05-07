package com.github.thundax.modules.member.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import java.util.Date;
import org.junit.Test;

public class MemberCredentialTest {

    @Test
    public void shouldLockWhenFailedLimitReached() {
        MemberCredential credential = new MemberCredential();
        credential.setFailedLimit(2);
        Date lockedUntil = new Date(System.currentTimeMillis() + 1000L);

        credential.markFailed(lockedUntil);
        credential.markFailed(lockedUntil);

        assertEquals(2, credential.getFailedCount());
        assertEquals(MemberCredentialStatus.LOCKED, credential.getStatus());
        assertTrue(credential.isLocked(new Date()));
    }

    @Test
    public void shouldMarkVerified() {
        MemberCredential credential = new MemberCredential();
        credential.setFailedCount(3);
        credential.lock(new Date(System.currentTimeMillis() + 1000L));
        Date verifiedAt = new Date();

        credential.markVerified(verifiedAt);

        assertEquals(0, credential.getFailedCount());
        assertEquals(MemberCredentialStatus.ACTIVE, credential.getStatus());
        assertEquals(verifiedAt, credential.getLastVerifiedAt());
    }
}
