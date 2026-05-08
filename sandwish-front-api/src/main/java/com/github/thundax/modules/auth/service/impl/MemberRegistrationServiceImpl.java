package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.MemberRegistrationService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final MemberService memberService;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final PreAuthSessionService preAuthSessionService;

    public MemberRegistrationServiceImpl(
            AuthProperties authProperties,
            MemberService memberService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            PreAuthSessionService preAuthSessionService) {
        this.memberService = memberService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.preAuthSessionService = preAuthSessionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerAccount(
            String loginToken, String name, String account, String encryptedPassword, String captcha)
            throws ApiException {
        requireText(name, "name");
        requireText(account, "account");
        requireText(encryptedPassword, "password");
        if (!preAuthSessionService.validateCaptcha(PrincipalType.MEMBER, loginToken, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_ACCOUNT, account);

        String password = preAuthSessionService.decryptRsaValue(PrincipalType.MEMBER, loginToken, encryptedPassword);
        requireText(password, "password");

        Member member = createMember(name);
        PrincipalIdentity identity = updateIdentity(member, PrincipalIdentityType.MEMBER_ACCOUNT, account);
        upsertPassword(member, identity, PasswordHelper.encrypt(password));
        preAuthSessionService.releasePreAuthSession(PrincipalType.MEMBER, loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterSmsCode(String loginToken, String mobile, String captcha) throws ApiException {
        requireText(mobile, "mobile");
        if (!preAuthSessionService.validateCaptcha(PrincipalType.MEMBER, loginToken, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        preAuthSessionService.createSmsValidateCode(PrincipalType.MEMBER, loginToken, mobile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerMobile(String loginToken, String name, String mobile, String validateCode)
            throws ApiException {
        requireText(name, "name");
        requireText(mobile, "mobile");
        if (!preAuthSessionService.validateSmsValidateCode(PrincipalType.MEMBER, loginToken, mobile, validateCode)) {
            throw new ApiException("短信验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_MOBILE, mobile);
        preAuthSessionService.releasePreAuthSession(PrincipalType.MEMBER, loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterEmailCode(String loginToken, String email, String captcha) throws ApiException {
        requireText(email, "email");
        if (!preAuthSessionService.validateCaptcha(PrincipalType.MEMBER, loginToken, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);
        preAuthSessionService.createEmailValidateCode(PrincipalType.MEMBER, loginToken, email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerEmail(String loginToken, String name, String email, String validateCode)
            throws ApiException {
        requireText(name, "name");
        requireText(email, "email");
        if (!preAuthSessionService.validateEmailValidateCode(PrincipalType.MEMBER, loginToken, email, validateCode)) {
            throw new ApiException("邮箱验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_EMAIL, email);
        preAuthSessionService.releasePreAuthSession(PrincipalType.MEMBER, loginToken);
        return member.getId();
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setStatus(MemberStatus.ACTIVE);
        member.setId(memberService.add(member));
        return member;
    }

    private void ensureIdentityAvailable(PrincipalIdentityType identityType, String identityValue) throws ApiException {
        if (principalIdentityService.getByIdentity(identityType, identityValue) != null) {
            throw new ApiException("会员标识已存在");
        }
    }

    private PrincipalIdentity updateIdentity(Member member, PrincipalIdentityType identityType, String identityValue) {
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.MEMBER, member.getId());
        PrincipalIdentity identity = principalIdentityService.getByPrincipalKeyAndType(principalKey, identityType);
        if (identity == null) {
            identity = new PrincipalIdentity();
            identity.setPrincipalKey(principalKey);
            identity.setType(identityType);
            identity.setIdentityValue(identityValue);
            identity.setStatus(PrincipalIdentityStatus.ENABLED);
            principalIdentityService.add(identity);
            return identity;
        }

        identity.setIdentityValue(identityValue);
        identity.setStatus(PrincipalIdentityStatus.ENABLED);
        principalIdentityService.update(identity);
        return identity;
    }

    private void upsertPassword(Member member, PrincipalIdentity identity, String encryptedPassword) {
        if (member == null || identity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.getByIdentityIdAndType(
                identity.getId(), PrincipalCredentialType.MEMBER_PASSWORD);
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.MEMBER, member.getId()));
            credential.setIdentityId(identity.getId());
            credential.setCredentialType(PrincipalCredentialType.MEMBER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.add(credential);
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.update(credential);
    }

    private void requireText(String value, String field) throws ApiException {
        if (StringUtils.isBlank(value)) {
            throw new ApiException(field + "不能为空");
        }
    }
}
