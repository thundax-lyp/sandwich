package com.github.thundax.modules.auth.entity;

import static org.junit.Assert.*;

import com.github.thundax.modules.auth.entity.enums.*;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.Date;
import org.junit.Test;

public class PrincipalIdentityCredentialTest {

    @Test
    public void shouldBuildPrincipalKey() {
        PrincipalKey userKey = PrincipalKey.of(PrincipalType.USER, 1L);
        PrincipalKey memberKey = PrincipalKey.of(PrincipalType.MEMBER, 2L);

        assertEquals(PrincipalType.USER, userKey.getPrincipalType());
        assertEquals(PrincipalType.MEMBER, memberKey.getPrincipalType());
    }

    @Test
    public void shouldRepresentPrincipalIdentityStateAndKind() {
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setType(PrincipalIdentityType.USER_MOBILE);
        identity.setIdentityValue("13800000000");

        assertTrue(identity.isEnabled());
        assertTrue(identity.isMobile());
        assertFalse(identity.isAccount());
        assertTrue(identity.matches("13800000000"));

        identity.disable();

        assertEquals(PrincipalIdentityStatus.DISABLED, identity.getStatus());
        assertTrue(identity.isDisabled());
    }

    @Test
    public void shouldRepresentPrincipalCredentialStateTransitions() {
        Date now = new Date();
        Date later = new Date(now.getTime() + 1000L);
        PrincipalCredential credential = new PrincipalCredential();
        credential.setCredentialType(PrincipalCredentialType.MEMBER_PASSWORD);
        credential.setFailedLimit(2);

        assertTrue(credential.isPassword());
        assertTrue(credential.isActive());

        credential.markFailed(later);
        assertEquals(1, credential.getFailedCount());
        assertFalse(credential.isLocked(now));

        credential.markFailed(later);
        assertEquals(PrincipalCredentialStatus.LOCKED, credential.getStatus());
        assertTrue(credential.isLocked(now));

        credential.markVerified(now);
        assertEquals(0, credential.getFailedCount());
        assertTrue(credential.isActive());
        assertEquals(now, credential.getLastVerifiedAt());

        credential.expire();
        assertTrue(credential.isExpired(now));

        credential.disable();
        assertEquals(PrincipalCredentialStatus.DISABLED, credential.getStatus());
    }
}
