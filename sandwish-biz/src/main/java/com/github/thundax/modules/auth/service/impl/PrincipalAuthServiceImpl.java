package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class PrincipalAuthServiceImpl implements PrincipalAuthService {

    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;

    public PrincipalAuthServiceImpl(
            PrincipalIdentityService principalIdentityService, PrincipalCredentialService principalCredentialService) {
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
    }

    @Override
    public PrincipalIdentity authenticateIdentity(PrincipalIdentityType identityType, String identityValue)
            throws ApiException {
        PrincipalIdentity identity = principalIdentityService.getByIdentity(identityType, identityValue);
        if (identity == null || !identity.isEnabled()) {
            throw invalidPrincipalCredential();
        }
        return identity;
    }

    @Override
    public PrincipalIdentity authenticatePassword(
            PrincipalIdentityType identityType,
            String identityValue,
            PrincipalCredentialType credentialType,
            String plainPassword,
            PrincipalPasswordPolicyDTO passwordPolicy)
            throws ApiException {
        PrincipalIdentity identity = authenticateIdentity(identityType, identityValue);
        PrincipalCredential credential =
                principalCredentialService.getByIdentityIdAndType(identity.getId(), credentialType);
        if (credential == null) {
            throw invalidPrincipalCredential();
        }
        validateCredential(credential, plainPassword, effectivePolicy(passwordPolicy));
        return identity;
    }

    private void validateCredential(
            PrincipalCredential credential, String plainPassword, PrincipalPasswordPolicyDTO passwordPolicy)
            throws ApiException {
        Date now = new Date();
        if (credential.isLocked(now)) {
            throw new ApiException("帐号已被锁定，请等待（" + lockedExpireSeconds(credential, passwordPolicy, now) + "）秒后自动解锁!");
        }
        if (credential.isExpired(now)) {
            throw new ApiException("认证凭据已过期");
        }
        if (!credential.isActive()) {
            throw new ApiException("认证凭据不可用");
        }

        if (PasswordHelper.validate(plainPassword, credential.getCredentialValue())) {
            credential.markVerified(now);
            principalCredentialService.updateVerifyState(credential);
            return;
        }

        if (!passwordPolicy.isLockEnabled()) {
            throw invalidPrincipalCredential();
        }

        if (credential.getFailedLimit() <= 0) {
            credential.setFailedLimit(passwordPolicy.getMaxFailedCount());
        }
        Date lockedUntil = new Date(now.getTime() + passwordPolicy.getLockSeconds() * 1000L);
        credential.markFailed(lockedUntil);
        principalCredentialService.updateVerifyState(credential);
        if (credential.isLocked(now)) {
            throw new ApiException("帐号已被锁定，请等待（" + passwordPolicy.getLockSeconds() + "）秒后自动解锁!");
        }
        throw new ApiException("密码输入错误"
                + credential.getFailedLimit()
                + "次后将被锁定，剩余"
                + (credential.getFailedLimit() - credential.getFailedCount())
                + "次");
    }

    private PrincipalPasswordPolicyDTO effectivePolicy(PrincipalPasswordPolicyDTO passwordPolicy) {
        return passwordPolicy == null ? PrincipalPasswordPolicyDTO.disabled() : passwordPolicy;
    }

    private long lockedExpireSeconds(
            PrincipalCredential credential, PrincipalPasswordPolicyDTO passwordPolicy, Date now) {
        if (credential.getLockedUntil() == null) {
            return passwordPolicy.getLockSeconds();
        }
        long remaining = (credential.getLockedUntil().getTime() - now.getTime()) / 1000L;
        return Math.max(remaining, 0L);
    }

    private ApiException invalidPrincipalCredential() {
        return new InvalidPasswordException();
    }
}
