package com.github.thundax.modules.auth.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import java.util.List;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

public class PrincipalAuthServiceImplTest {

    @BeforeClass
    public static void setUpMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("auth.exception.invalid-password", Locale.getDefault(), "bad password");
        new I18nMessages(messageSource);
    }

    @Test
    public void shouldAuthenticatePasswordAndResetVerifyState() throws ApiException {
        RecordingPrincipalIdentityService identityService = new RecordingPrincipalIdentityService();
        RecordingPrincipalCredentialService credentialService = new RecordingPrincipalCredentialService();
        PrincipalIdentity identity = identity(1001L, "developer", PrincipalIdentityStatus.ENABLED);
        PrincipalCredential credential = credential(2001L, identity.getId(), PasswordHelper.encrypt("Q1w2e3r$"));
        credential.setFailedCount(2);
        identityService.identity = identity;
        credentialService.credential = credential;

        PrincipalAuthServiceImpl service = new PrincipalAuthServiceImpl(identityService, credentialService);

        PrincipalIdentity result = service.authenticatePassword(
                PrincipalIdentityType.USER_ACCOUNT,
                "developer",
                PrincipalCredentialType.USER_PASSWORD,
                "Q1w2e3r$",
                PrincipalPasswordPolicyDTO.disabled());

        assertSame(identity, result);
        assertEquals(Integer.valueOf(1), credentialService.updateVerifyStateCalls);
        assertEquals(0, credential.getFailedCount());
        assertEquals(PrincipalCredentialStatus.ACTIVE, credential.getStatus());
        assertNotNull(credential.getLastVerifiedAt());
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldRejectWrongPasswordWithoutLockPolicy() throws ApiException {
        RecordingPrincipalIdentityService identityService = new RecordingPrincipalIdentityService();
        RecordingPrincipalCredentialService credentialService = new RecordingPrincipalCredentialService();
        PrincipalIdentity identity = identity(1001L, "developer", PrincipalIdentityStatus.ENABLED);
        identityService.identity = identity;
        credentialService.credential = credential(2001L, identity.getId(), PasswordHelper.encrypt("Q1w2e3r$"));

        PrincipalAuthServiceImpl service = new PrincipalAuthServiceImpl(identityService, credentialService);

        service.authenticatePassword(
                PrincipalIdentityType.USER_ACCOUNT,
                "developer",
                PrincipalCredentialType.USER_PASSWORD,
                "wrong",
                PrincipalPasswordPolicyDTO.disabled());
    }

    @Test
    public void shouldRecordFailedPasswordAndLockWhenPolicyReached() {
        RecordingPrincipalIdentityService identityService = new RecordingPrincipalIdentityService();
        RecordingPrincipalCredentialService credentialService = new RecordingPrincipalCredentialService();
        PrincipalIdentity identity = identity(1001L, "developer", PrincipalIdentityStatus.ENABLED);
        PrincipalCredential credential = credential(2001L, identity.getId(), PasswordHelper.encrypt("Q1w2e3r$"));
        credential.setFailedCount(1);
        identityService.identity = identity;
        credentialService.credential = credential;
        PrincipalAuthServiceImpl service = new PrincipalAuthServiceImpl(identityService, credentialService);

        try {
            service.authenticatePassword(
                    PrincipalIdentityType.USER_ACCOUNT,
                    "developer",
                    PrincipalCredentialType.USER_PASSWORD,
                    "wrong",
                    new PrincipalPasswordPolicyDTO(true, 2, 60));
        } catch (ApiException e) {
            assertEquals("帐号已被锁定，请等待（60）秒后自动解锁!", e.getMessage());
        }

        assertEquals(Integer.valueOf(1), credentialService.updateVerifyStateCalls);
        assertEquals(2, credential.getFailedCount());
        assertEquals(2, credential.getFailedLimit());
        assertEquals(PrincipalCredentialStatus.LOCKED, credential.getStatus());
        assertNotNull(credential.getLockedUntil());
    }

    private static PrincipalIdentity identity(long id, String identityValue, PrincipalIdentityStatus status) {
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setId(EntityIdCodec.toDomain(id));
        identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(1L)));
        identity.setType(PrincipalIdentityType.USER_ACCOUNT);
        identity.setIdentityValue(identityValue);
        identity.setStatus(status);
        return identity;
    }

    private static PrincipalCredential credential(long id, EntityId identityId, String password) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setId(EntityIdCodec.toDomain(id));
        credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityIdCodec.toDomain(1L)));
        credential.setIdentityId(identityId);
        credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
        credential.setCredentialValue(password);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        return credential;
    }

    private static class RecordingPrincipalIdentityService implements PrincipalIdentityService {
        private PrincipalIdentity identity;

        @Override
        public PrincipalIdentity getById(EntityId id) {
            return identity;
        }

        @Override
        public PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue) {
            if (identity != null && identity.getType() == identityType && identity.matches(identityValue)) {
                return identity;
            }
            return null;
        }

        @Override
        public PrincipalIdentity getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalIdentityType identityType) {
            return identity;
        }

        @Override
        public List<PrincipalIdentity> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalIdentityStatus status) {
            return null;
        }

        @Override
        public EntityId add(PrincipalIdentity principalIdentity) {
            return principalIdentity.getId();
        }

        @Override
        public void update(PrincipalIdentity principalIdentity) {}

        @Override
        public void updateStatus(PrincipalIdentity principalIdentity) {}
    }

    private static class RecordingPrincipalCredentialService implements PrincipalCredentialService {
        private PrincipalCredential credential;
        private Integer updateVerifyStateCalls = 0;

        @Override
        public PrincipalCredential getById(EntityId id) {
            return credential;
        }

        @Override
        public PrincipalCredential getByIdentityIdAndType(EntityId identityId, PrincipalCredentialType credentialType) {
            if (credential != null
                    && credential.getIdentityId().equals(identityId)
                    && credential.getCredentialType() == credentialType) {
                return credential;
            }
            return null;
        }

        @Override
        public PrincipalCredential getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalCredentialType credentialType) {
            return credential;
        }

        @Override
        public List<PrincipalCredential> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalCredentialStatus status) {
            return null;
        }

        @Override
        public EntityId add(PrincipalCredential principalCredential) {
            return principalCredential.getId();
        }

        @Override
        public void update(PrincipalCredential principalCredential) {}

        @Override
        public void updateStatus(PrincipalCredential principalCredential) {}

        @Override
        public void updateVerifyState(PrincipalCredential principalCredential) {
            updateVerifyStateCalls++;
        }
    }
}
