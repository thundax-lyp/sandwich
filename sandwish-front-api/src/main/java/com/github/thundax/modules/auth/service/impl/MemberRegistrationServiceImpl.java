package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.MemberRegistrationService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    private static final int SMS_VALIDATE_CODE_LENGTH = 6;
    private static final int EMAIL_VALIDATE_CODE_LENGTH = 6;
    private static final char[] VALIDATE_CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final String PRIVATE_KEY_SEPARATOR = ":";
    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final AuthProperties authProperties;
    private final MemberService memberService;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final MemberLoginFormDao memberLoginFormDao;

    public MemberRegistrationServiceImpl(
            AuthProperties authProperties,
            MemberService memberService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            MemberLoginFormDao memberLoginFormDao) {
        this.authProperties = authProperties;
        this.memberService = memberService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.memberLoginFormDao = memberLoginFormDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerAccount(
            String loginToken, String name, String account, String encryptedPassword, String captcha)
            throws ApiException {
        requireText(name, "name");
        requireText(account, "account");
        requireText(encryptedPassword, "password");
        validateCaptcha(loginToken, captcha);
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_ACCOUNT, account);

        String password = decryptPassword(loginToken, encryptedPassword);
        requireText(password, "password");

        Member member = createMember(name);
        PrincipalIdentity identity = updateIdentity(member, PrincipalIdentityType.MEMBER_ACCOUNT, account);
        upsertPassword(member, identity, PasswordHelper.encrypt(password));
        memberLoginFormDao.deleteByToken(loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterSmsCode(String loginToken, String mobile, String captcha) throws ApiException {
        requireText(mobile, "mobile");
        validateCaptcha(loginToken, captcha);
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        memberLoginFormDao.updateSmsValidateCode(
                loginToken, mobile, createCode(VALIDATE_CODE, SMS_VALIDATE_CODE_LENGTH));
        memberLoginFormDao.updateCaptcha(loginToken, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerMobile(String loginToken, String name, String mobile, String validateCode)
            throws ApiException {
        requireText(name, "name");
        requireText(mobile, "mobile");
        validateMobileCode(loginToken, mobile, validateCode);
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_MOBILE, mobile);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_MOBILE, mobile);
        memberLoginFormDao.deleteByToken(loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterEmailCode(String loginToken, String email, String captcha) throws ApiException {
        requireText(email, "email");
        validateCaptcha(loginToken, captcha);
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);
        memberLoginFormDao.updateEmailValidateCode(
                loginToken, email, createCode(VALIDATE_CODE, EMAIL_VALIDATE_CODE_LENGTH));
        memberLoginFormDao.updateCaptcha(loginToken, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId registerEmail(String loginToken, String name, String email, String validateCode)
            throws ApiException {
        requireText(name, "name");
        requireText(email, "email");
        validateEmailCode(loginToken, email, validateCode);
        ensureIdentityAvailable(PrincipalIdentityType.MEMBER_EMAIL, email);

        Member member = createMember(name);
        updateIdentity(member, PrincipalIdentityType.MEMBER_EMAIL, email);
        memberLoginFormDao.deleteByToken(loginToken);
        return member.getId();
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setStatus(MemberStatus.ACTIVE);
        member.setId(memberService.add(member));
        return member;
    }

    private void validateCaptcha(String loginToken, String captcha) throws ApiException {
        MemberLoginForm form = getLoginForm(loginToken);
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), captcha)) {
            return;
        }
        if (form.isNullCaptcha() || !StringUtils.equals(form.getCaptcha(), captcha)) {
            throw new ApiException("图形验证码错误");
        }
    }

    private void validateMobileCode(String loginToken, String mobile, String validateCode) throws ApiException {
        MemberLoginForm form = getLoginForm(loginToken);
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return;
        }
        if (!StringUtils.equals(form.getMobile(), mobile)
                || !StringUtils.equals(form.getMobileValidateCode(), validateCode)) {
            throw new ApiException("短信验证码错误");
        }
    }

    private void validateEmailCode(String loginToken, String email, String validateCode) throws ApiException {
        MemberLoginForm form = getLoginForm(loginToken);
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return;
        }
        if (!StringUtils.equals(form.getEmail(), email)
                || !StringUtils.equals(form.getEmailValidateCode(), validateCode)) {
            throw new ApiException("邮箱验证码错误");
        }
    }

    private MemberLoginForm getLoginForm(String loginToken) throws ApiException {
        requireText(loginToken, "loginToken");
        MemberLoginForm form = memberLoginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new ApiException("登录表单已失效");
        }
        return form;
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

    private String decryptPassword(String loginToken, String encryptedPassword) throws ApiException {
        MemberLoginForm form = getLoginForm(loginToken);
        if (StringUtils.isBlank(form.getPrivateKey())) {
            throw new ApiException("登录表单密钥已失效");
        }
        String[] privateKeyParts = StringUtils.split(form.getPrivateKey(), PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            throw new ApiException("登录表单密钥已失效");
        }
        RSAUtils.ReadableKeyPair keyPair =
                new RSAUtils.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RSAUtils.decryptBase64(encryptedPassword, keyPair);
    }

    private void requireText(String value, String field) throws ApiException {
        if (StringUtils.isBlank(value)) {
            throw new ApiException(field + "不能为空");
        }
    }

    private String createCode(char[] candidate, int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(candidate[random.nextInt(candidate.length)]);
        }
        return builder.toString();
    }
}
