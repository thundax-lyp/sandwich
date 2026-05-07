package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import com.github.thundax.modules.auth.service.MemberRegistrationService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberCredentialService;
import com.github.thundax.modules.member.service.MemberIdentityService;
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

    private final AuthProperties authProperties;
    private final MemberService memberService;
    private final MemberIdentityService memberIdentityService;
    private final MemberCredentialService memberCredentialService;
    private final MemberLoginFormDao memberLoginFormDao;
    private final PasswordService passwordService;

    public MemberRegistrationServiceImpl(
            AuthProperties authProperties,
            MemberService memberService,
            MemberIdentityService memberIdentityService,
            MemberCredentialService memberCredentialService,
            MemberLoginFormDao memberLoginFormDao,
            PasswordService passwordService) {
        this.authProperties = authProperties;
        this.memberService = memberService;
        this.memberIdentityService = memberIdentityService;
        this.memberCredentialService = memberCredentialService;
        this.memberLoginFormDao = memberLoginFormDao;
        this.passwordService = passwordService;
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
        ensureIdentityAvailable(MemberIdentityType.ACCOUNT, account);

        String password = decryptPassword(loginToken, encryptedPassword);
        requireText(password, "password");

        Member member = createMember(name);
        MemberIdentity identity = memberIdentityService.updateIdentity(member, MemberIdentityType.ACCOUNT, account);
        memberCredentialService.upsertPassword(member, identity, passwordService.encrypt(password));
        memberLoginFormDao.deleteByToken(loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterSmsCode(String loginToken, String mobile, String captcha) throws ApiException {
        requireText(mobile, "mobile");
        validateCaptcha(loginToken, captcha);
        ensureIdentityAvailable(MemberIdentityType.MOBILE, mobile);
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
        ensureIdentityAvailable(MemberIdentityType.MOBILE, mobile);

        Member member = createMember(name);
        memberIdentityService.updateIdentity(member, MemberIdentityType.MOBILE, mobile);
        memberLoginFormDao.deleteByToken(loginToken);
        return member.getId();
    }

    @Override
    public void sendRegisterEmailCode(String loginToken, String email, String captcha) throws ApiException {
        requireText(email, "email");
        validateCaptcha(loginToken, captcha);
        ensureIdentityAvailable(MemberIdentityType.EMAIL, email);
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
        ensureIdentityAvailable(MemberIdentityType.EMAIL, email);

        Member member = createMember(name);
        memberIdentityService.updateIdentity(member, MemberIdentityType.EMAIL, email);
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

    private void ensureIdentityAvailable(MemberIdentityType identityType, String identityValue) throws ApiException {
        if (memberIdentityService.getByIdentity(identityType, identityValue) != null) {
            throw new ApiException("会员标识已存在");
        }
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
