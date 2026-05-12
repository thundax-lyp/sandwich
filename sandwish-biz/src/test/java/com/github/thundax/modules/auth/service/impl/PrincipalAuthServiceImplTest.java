package com.github.thundax.modules.auth.service.impl;

import static org.junit.Assert.*;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.*;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalCredentialId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalCredentialIdCodec;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityIdCodec;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.command.AuthenticatePasswordCommand;
import com.github.thundax.modules.auth.service.command.PrincipalCredentialCommand;
import com.github.thundax.modules.auth.service.command.PrincipalIdentityCommand;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import java.util.List;
import org.junit.Test;

public class PrincipalAuthServiceImplTest {

    @Test
    public void shouldAuthenticatePasswordAndResetVerifyState() {
        RecordingPrincipalIdentityService identityService = new RecordingPrincipalIdentityService();
        RecordingPrincipalCredentialService credentialService = new RecordingPrincipalCredentialService();
        PrincipalIdentity identity = identity(1001L, "developer", PrincipalIdentityStatus.ENABLED);
        PrincipalCredential credential = credential(2001L, identity.getId(), PasswordHelper.encrypt("Q1w2e3r$"));
        credential.setFailedCount(2);
        identityService.identity = identity;
        credentialService.credential = credential;

        PrincipalAuthServiceImpl service = new PrincipalAuthServiceImpl(identityService, credentialService);

        PrincipalIdentity result = service.authenticatePassword(new AuthenticatePasswordCommand(
                PrincipalIdentityType.USER_ACCOUNT,
                "developer",
                PrincipalCredentialType.USER_PASSWORD,
                "Q1w2e3r$",
                PrincipalPasswordPolicyDTO.disabled()));

        assertSame(identity, result);
        assertEquals(Integer.valueOf(1), credentialService.updateVerifyStateCalls);
        assertEquals(0, credential.getFailedCount());
        assertEquals(PrincipalCredentialStatus.ACTIVE, credential.getStatus());
        assertNotNull(credential.getLastVerifiedAt());
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldRejectWrongPasswordWithoutLockPolicy() {
        RecordingPrincipalIdentityService identityService = new RecordingPrincipalIdentityService();
        RecordingPrincipalCredentialService credentialService = new RecordingPrincipalCredentialService();
        PrincipalIdentity identity = identity(1001L, "developer", PrincipalIdentityStatus.ENABLED);
        identityService.identity = identity;
        credentialService.credential = credential(2001L, identity.getId(), PasswordHelper.encrypt("Q1w2e3r$"));

        PrincipalAuthServiceImpl service = new PrincipalAuthServiceImpl(identityService, credentialService);

        service.authenticatePassword(new AuthenticatePasswordCommand(
                PrincipalIdentityType.USER_ACCOUNT,
                "developer",
                PrincipalCredentialType.USER_PASSWORD,
                "wrong",
                PrincipalPasswordPolicyDTO.disabled()));
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
            service.authenticatePassword(new AuthenticatePasswordCommand(
                    PrincipalIdentityType.USER_ACCOUNT,
                    "developer",
                    PrincipalCredentialType.USER_PASSWORD,
                    "wrong",
                    new PrincipalPasswordPolicyDTO(true, 2, 60)));
        } catch (BizException e) {
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
        identity.setId(PrincipalIdentityIdCodec.toDomain(id));
        identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
        identity.setType(PrincipalIdentityType.USER_ACCOUNT);
        identity.setIdentityValue(identityValue);
        identity.setStatus(status);
        return identity;
    }

    private static PrincipalCredential credential(long id, PrincipalIdentityId identityId, String password) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setId(PrincipalCredentialIdCodec.toDomain(id));
        credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1L));
        credential.setIdentityId(identityId);
        credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
        credential.setCredentialValue(password);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        return credential;
    }

    private static class RecordingPrincipalIdentityService implements PrincipalIdentityService {
        private PrincipalIdentity identity;

        @Override
        public PrincipalIdentity get(PrincipalIdentityQuery query) {
            if (query.getId() != null) {
                return identity;
            }
            if (identity != null
                    && identity.getType() == query.getIdentityType()
                    && identity.matches(query.getIdentityValue())) {
                return identity;
            }
            if (query.getPrincipalKey() != null && query.getIdentityType() != null) {
                return identity;
            }
            return null;
        }

        @Override
        public List<PrincipalIdentity> list(PrincipalIdentityQuery query) {
            return null;
        }

        @Override
        public PrincipalIdentityId create(PrincipalIdentityCommand command) {
            return command.getPrincipalIdentity().getId();
        }

        @Override
        public void change(PrincipalIdentityCommand command) {}

        @Override
        public void changeStatus(PrincipalIdentityCommand command) {}
    }

    private static class RecordingPrincipalCredentialService implements PrincipalCredentialService {
        private PrincipalCredential credential;
        private Integer updateVerifyStateCalls = 0;

        @Override
        public PrincipalCredential get(PrincipalCredentialQuery query) {
            if (query.getId() != null) {
                return credential;
            }
            if (credential != null
                    && credential.getIdentityId().equals(query.getIdentityId())
                    && credential.getCredentialType() == query.getCredentialType()) {
                return credential;
            }
            if (query.getPrincipalKey() != null && query.getCredentialType() != null) {
                return credential;
            }
            return null;
        }

        @Override
        public List<PrincipalCredential> list(PrincipalCredentialQuery query) {
            return null;
        }

        @Override
        public PrincipalCredentialId create(PrincipalCredentialCommand command) {
            return command.getPrincipalCredential().getId();
        }

        @Override
        public void change(PrincipalCredentialCommand command) {}

        @Override
        public void changeStatus(PrincipalCredentialCommand command) {}

        @Override
        public void changeVerifyState(PrincipalCredentialCommand command) {
            updateVerifyStateCalls++;
        }
    }
}
