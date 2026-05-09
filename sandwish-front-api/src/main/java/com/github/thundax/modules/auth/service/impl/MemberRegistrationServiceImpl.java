package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.MemberRegistrationService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.command.PrincipalCredentialCommand;
import com.github.thundax.modules.auth.service.command.PrincipalIdentityCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.command.MemberRegistrationCommand;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberService;
import com.github.thundax.modules.member.service.command.MemberCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final String EMAIL_ITEM = "EMAIL";
    private static final String EMAIL_VALIDATE_CODE_ITEM = "EMAIL_VALIDATE_CODE";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String MEMBER_PRIVATE_KEY_SEPARATOR = ":";

    private final AuthProperties authProperties;
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
        this.authProperties = authProperties;
        this.memberService = memberService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.preAuthSessionService = preAuthSessionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerAccount(MemberRegistrationCommand command) throws ApiException {
        String loginToken = command.getLoginToken();
        String name = command.getName();
        String account = command.getAccount();
        String encryptedPassword = command.getEncryptedPassword();
        String captcha = command.getCaptcha();
        requireText(name, "name");
        requireText(account, "account");
        requireText(encryptedPassword, "password");
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateCaptcha(token, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_ACCOUNT, account);

        String password = decryptRsaValue(token, encryptedPassword);
        requireText(password, "password");

        Member member = createMember(name);
        PrincipalIdentity identity = updateIdentity(member, PrincipalIdentityType.MEMBER_ACCOUNT, account);
        upsertPassword(member, identity, PasswordHelper.encrypt(password));
        preAuthSessionService.release(new ReleasePreAuthSessionCommand(requireSessionId(token)));
        return member.getId();
    }

    @Override
    public void sendRegisterSmsCode(MemberRegistrationCommand command) throws ApiException {
        String loginToken = command.getLoginToken();
        String mobile = command.getMobile();
        String captcha = command.getCaptcha();
        requireText(mobile, "mobile");
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateCaptcha(token, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        String validateCode = PreAuthCodeHelper.generateSmsCode();
        PreAuthSessionId sessionId = requireSessionId(token);
        long expiredAt = preAuthSessionService.get(new PreAuthSessionQuery(sessionId, null, null, null)).getExpiredAt();
        preAuthSessionService.upsertValue(
                new UpsertPreAuthSessionValueCommand(sessionId, SMS_MOBILE_ITEM, mobile, expiredAt));
        preAuthSessionService.upsertValue(
                new UpsertPreAuthSessionValueCommand(sessionId, SMS_VALIDATE_CODE_ITEM, validateCode, expiredAt));
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(sessionId, CAPTCHA_ITEM, null, 0L));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerMobile(MemberRegistrationCommand command) throws ApiException {
        String loginToken = command.getLoginToken();
        String name = command.getName();
        String mobile = command.getMobile();
        String validateCode = command.getValidateCode();
        requireText(name, "name");
        requireText(mobile, "mobile");
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateSmsValidateCode(token, mobile, validateCode)) {
            throw new ApiException("短信验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_MOBILE, mobile);
        preAuthSessionService.release(new ReleasePreAuthSessionCommand(requireSessionId(token)));
        return member.getId();
    }

    @Override
    public void sendRegisterEmailCode(MemberRegistrationCommand command) throws ApiException {
        String loginToken = command.getLoginToken();
        String email = command.getEmail();
        String captcha = command.getCaptcha();
        requireText(email, "email");
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateCaptcha(token, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);
        String validateCode = PreAuthCodeHelper.generateEmailCode();
        PreAuthSessionId sessionId = requireSessionId(token);
        long expiredAt = preAuthSessionService.get(new PreAuthSessionQuery(sessionId, null, null, null)).getExpiredAt();
        preAuthSessionService.upsertValue(
                new UpsertPreAuthSessionValueCommand(sessionId, EMAIL_ITEM, email, expiredAt));
        preAuthSessionService.upsertValue(
                new UpsertPreAuthSessionValueCommand(sessionId, EMAIL_VALIDATE_CODE_ITEM, validateCode, expiredAt));
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(sessionId, CAPTCHA_ITEM, null, 0L));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerEmail(MemberRegistrationCommand command) throws ApiException {
        String loginToken = command.getLoginToken();
        String name = command.getName();
        String email = command.getEmail();
        String validateCode = command.getValidateCode();
        requireText(name, "name");
        requireText(email, "email");
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateEmailValidateCode(token, email, validateCode)) {
            throw new ApiException("邮箱验证码错误");
        }
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_EMAIL, email);
        preAuthSessionService.release(new ReleasePreAuthSessionCommand(requireSessionId(token)));
        return member.getId();
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setStatus(MemberStatus.ACTIVE);
        member.setId(memberService.create(new MemberCommand(null, member)));
        return member;
    }

    private void ensureIdentityAvailable(PrincipalIdentityType identityType, String identityValue) throws ApiException {
        if (principalIdentityService.get(identityQuery(identityType, identityValue)) != null) {
            throw new ApiException("会员标识已存在");
        }
    }

    private PrincipalIdentity updateIdentity(Member member, PrincipalIdentityType identityType, String identityValue) {
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.MEMBER, member.getId());
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(principalKey, identityType));
        if (identity == null) {
            identity = new PrincipalIdentity();
            identity.setPrincipalKey(principalKey);
            identity.setType(identityType);
            identity.setIdentityValue(identityValue);
            identity.setStatus(PrincipalIdentityStatus.ENABLED);
            principalIdentityService.create(new PrincipalIdentityCommand(identity));
            return identity;
        }

        identity.setIdentityValue(identityValue);
        identity.setStatus(PrincipalIdentityStatus.ENABLED);
        principalIdentityService.change(new PrincipalIdentityCommand(identity));
        return identity;
    }

    private void upsertPassword(Member member, PrincipalIdentity identity, String encryptedPassword) {
        if (member == null || identity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.get(
                credentialQuery(identity.getId(), PrincipalCredentialType.MEMBER_PASSWORD));
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
            principalCredentialService.create(new PrincipalCredentialCommand(credential));
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.change(new PrincipalCredentialCommand(credential));
    }

    private PrincipalIdentityQuery identityQuery(PrincipalIdentityType identityType, String identityValue) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setIdentityType(identityType);
        query.setIdentityValue(identityValue);
        return query;
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(EntityId identityId, PrincipalCredentialType credentialType) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identityId);
        query.setCredentialType(credentialType);
        return query;
    }

    private void requireText(String value, String field) throws ApiException {
        if (StringUtils.isBlank(value)) {
            throw new ApiException(field + "不能为空");
        }
    }

    private boolean validateCaptcha(PreAuthSessionToken token, String captcha) throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(
                captcha,
                preAuthSessionService.getValue(new PreAuthSessionQuery(requireSessionId(token), null, null, CAPTCHA_ITEM)));
    }

    private boolean validateSmsValidateCode(PreAuthSessionToken token, String mobile, String validateCode)
            throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionId(token);
        return StringUtils.equals(
                        preAuthSessionService.getValue(new PreAuthSessionQuery(sessionId, null, null, SMS_MOBILE_ITEM)),
                        mobile)
                && StringUtils.equals(
                        preAuthSessionService.getValue(
                                new PreAuthSessionQuery(sessionId, null, null, SMS_VALIDATE_CODE_ITEM)),
                        validateCode);
    }

    private boolean validateEmailValidateCode(PreAuthSessionToken token, String email, String validateCode)
            throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionId(token);
        return StringUtils.equals(
                        preAuthSessionService.getValue(new PreAuthSessionQuery(sessionId, null, null, EMAIL_ITEM)),
                        email)
                && StringUtils.equals(
                        preAuthSessionService.getValue(
                                new PreAuthSessionQuery(sessionId, null, null, EMAIL_VALIDATE_CODE_ITEM)),
                        validateCode);
    }

    private String decryptRsaValue(PreAuthSessionToken token, String encryptedValue) throws ApiException {
        String privateKey = preAuthSessionService.getValue(
                new PreAuthSessionQuery(requireSessionId(token), null, null, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw new ApiException("登录表单密钥已失效");
        }
        String[] privateKeyParts = StringUtils.split(privateKey, MEMBER_PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            throw new ApiException("登录表单密钥已失效");
        }
        RSAUtils.ReadableKeyPair keyPair =
                new RSAUtils.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RSAUtils.decryptBase64(encryptedValue, keyPair);
    }

    private PreAuthSessionId requireSessionId(PreAuthSessionToken token) throws ApiException {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(new PreAuthSessionQuery(null, token, null, null));
        if (sessionId == null) {
            throw new ApiException("登录表单已失效");
        }
        return sessionId;
    }
}
