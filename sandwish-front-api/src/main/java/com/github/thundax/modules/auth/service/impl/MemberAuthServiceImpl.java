package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.RSAUtils;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberAccessTokenDao;
import com.github.thundax.modules.auth.dao.MemberAuthSessionDao;
import com.github.thundax.modules.auth.dao.MemberAuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.MemberLoginFormDao;
import com.github.thundax.modules.auth.dao.MemberRefreshTokenDao;
import com.github.thundax.modules.auth.entity.MemberAccessToken;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.MemberLoginForm;
import com.github.thundax.modules.auth.entity.MemberRefreshToken;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.service.MemberCredentialService;
import com.github.thundax.modules.member.service.MemberIdentityService;
import com.github.thundax.modules.member.service.MemberService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberAuthServiceImpl implements MemberAuthService {
    private static final int CAPTCHA_LENGTH = 4;
    private static final char[] VALIDATE_CAPTCHA_CODE = {'2', '3', '4', '5', '6', '7', '8', '9'};
    private static final String PRIVATE_KEY_SEPARATOR = ":";

    private final AuthProperties authProperties;
    private final MemberLoginFormDao memberLoginFormDao;
    private final MemberIdentityService memberIdentityService;
    private final MemberCredentialService memberCredentialService;
    private final MemberService memberService;
    private final PasswordService passwordService;
    private final MemberAuthSessionDao memberAuthSessionDao;
    private final MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao;
    private final MemberAccessTokenDao memberAccessTokenDao;
    private final MemberRefreshTokenDao memberRefreshTokenDao;

    public MemberAuthServiceImpl(
            AuthProperties authProperties,
            MemberLoginFormDao memberLoginFormDao,
            MemberIdentityService memberIdentityService,
            MemberCredentialService memberCredentialService,
            MemberService memberService,
            PasswordService passwordService,
            MemberAuthSessionDao memberAuthSessionDao,
            MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao,
            MemberAccessTokenDao memberAccessTokenDao,
            MemberRefreshTokenDao memberRefreshTokenDao) {
        this.authProperties = authProperties;
        this.memberLoginFormDao = memberLoginFormDao;
        this.memberIdentityService = memberIdentityService;
        this.memberCredentialService = memberCredentialService;
        this.memberService = memberService;
        this.passwordService = passwordService;
        this.memberAuthSessionDao = memberAuthSessionDao;
        this.memberAuthSessionRuntimeDao = memberAuthSessionRuntimeDao;
        this.memberAccessTokenDao = memberAccessTokenDao;
        this.memberRefreshTokenDao = memberRefreshTokenDao;
    }

    @Override
    public MemberLoginForm createLoginForm() {
        MemberLoginForm form = new MemberLoginForm();
        form.setLoginToken(UuidHelper.compact());
        form.setRefreshTokenList(new ArrayList<>(Collections.singletonList(UuidHelper.compact())));
        form.setExpiredSeconds(authProperties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        form.setCaptcha(createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH));
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();
        form.setPublicKey(keyPair.getPublicKey());
        form.setPrivateKey(privateKeyValue(keyPair));
        memberLoginFormDao.insert(form);
        return form;
    }

    @Override
    public MemberLoginForm refreshLoginForm(String refreshToken) throws ApiException {
        MemberLoginForm form = memberLoginFormDao.getByRefreshToken(refreshToken);
        if (form == null || !form.validateCheckCode()) {
            throw new ApiException("登录表单已失效");
        }
        form.getRefreshTokenList().add(0, UuidHelper.compact());
        form.setLoginToken(UuidHelper.compact());
        form.setExpiredSeconds(authProperties.getLoginExpiredSeconds());
        form.setCheckCode(AuthUtils.currentCheckCode());
        memberLoginFormDao.insert(form);
        return form;
    }

    @Override
    public String createCaptcha(String loginToken) throws ApiException {
        if (!memberLoginFormDao.tokenExists(loginToken)) {
            throw new ApiException("登录表单已失效");
        }
        String captcha = createCode(VALIDATE_CAPTCHA_CODE, CAPTCHA_LENGTH);
        memberLoginFormDao.updateCaptcha(loginToken, captcha);
        return captcha;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginAccount(String loginToken, String account, String encryptedPassword, String captcha)
            throws ApiException {
        validateCaptcha(loginToken, captcha);
        MemberIdentity identity = requireIdentity(MemberIdentityType.ACCOUNT, account);
        MemberCredential credential = memberCredentialService.getPasswordCredential(identity.getMemberId());
        if (credential == null || !credential.isActive()) {
            throw new ApiException("用户名或密码错误");
        }
        String password = decryptPassword(loginToken, encryptedPassword);
        if (!passwordService.validate(password, credential.getCredentialValue())) {
            throw new ApiException("用户名或密码错误");
        }
        memberLoginFormDao.deleteByToken(loginToken);
        return createTokenResult(requireActiveMember(identity.getMemberId()), identity, "ACCOUNT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(String loginToken, String mobile, String validateCode) throws ApiException {
        validateSmsCode(loginToken, mobile, validateCode);
        MemberIdentity identity = requireIdentity(MemberIdentityType.MOBILE, mobile);
        memberLoginFormDao.deleteByToken(loginToken);
        return createTokenResult(requireActiveMember(identity.getMemberId()), identity, "SMS");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult refreshAccessToken(String refreshToken) throws ApiException {
        MemberRefreshToken oldRefreshToken = memberRefreshTokenDao.getByTokenHash(tokenHash(refreshToken));
        Date now = new Date();
        if (oldRefreshToken == null || !oldRefreshToken.canRefresh(now)) {
            throw new ApiException("refreshToken已失效");
        }
        oldRefreshToken.markUsed(now);
        memberRefreshTokenDao.update(oldRefreshToken);
        Member member = requireActiveMember(oldRefreshToken.getMemberId());
        MemberAuthSession session = memberAuthSessionDao.getBySessionId(oldRefreshToken.getSessionId());
        return createTokenResult(member, session, "REFRESH");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String accessToken) throws ApiException {
        MemberAccessToken token = memberAccessTokenDao.getByTokenHash(tokenHash(accessToken));
        if (token == null) {
            throw new ApiException("accessToken已失效");
        }
        Date now = new Date();
        token.revoke(now);
        memberAccessTokenDao.update(token);
        MemberAuthSession session = memberAuthSessionDao.getBySessionId(token.getSessionId());
        if (session != null) {
            session.logout(now);
            memberAuthSessionDao.update(session);
            memberAuthSessionRuntimeDao.deleteBySessionId(session.getSessionId());
        }
    }

    @Override
    public MemberAccessToken getValidAccessToken(String accessToken) {
        MemberAccessToken token = memberAccessTokenDao.getByTokenHash(tokenHash(accessToken));
        return token != null && token.canAccess(new Date()) ? token : null;
    }

    private MemberTokenResult createTokenResult(Member member, MemberIdentity identity, String loginType) {
        MemberAuthSession session = new MemberAuthSession();
        session.setSessionId(UuidHelper.compact());
        session.setMemberId(member.getId());
        session.setIdentityId(identity.getId());
        session.setIdentityType(identity.getIdentityType());
        session.setLoginType(loginType);
        return createTokenResult(member, session, loginType);
    }

    private MemberTokenResult createTokenResult(Member member, MemberAuthSession session, String loginType) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + authProperties.getLoginExpiredSeconds() * 1000L);
        session.setLoginType(loginType);
        session.setIssuedAt(now);
        session.setLastAccessTime(now);
        session.setExpireAt(expireAt);
        session.setCreateDate(now);
        if (session.getId() == null) {
            session.setId(memberAuthSessionDao.insert(session));
        }
        memberAuthSessionRuntimeDao.insert(session, session.remainingSeconds(now));

        String accessTokenValue = UuidHelper.compact();
        MemberAccessToken accessToken = new MemberAccessToken();
        accessToken.setTokenId(UuidHelper.compact());
        accessToken.setTokenHash(tokenHash(accessTokenValue));
        accessToken.setSessionId(session.getSessionId());
        accessToken.setMemberId(member.getId());
        accessToken.setIssuedAt(now);
        accessToken.setExpireAt(expireAt);
        accessToken.setStatus(MemberAccessTokenStatus.ACTIVE);
        accessToken.setCreateDate(now);
        memberAccessTokenDao.insert(accessToken);

        String refreshTokenValue = UuidHelper.compact();
        MemberRefreshToken refreshToken = new MemberRefreshToken();
        refreshToken.setTokenId(UuidHelper.compact());
        refreshToken.setTokenHash(tokenHash(refreshTokenValue));
        refreshToken.setAccessTokenId(accessToken.getTokenId());
        refreshToken.setSessionId(session.getSessionId());
        refreshToken.setMemberId(member.getId());
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(new Date(now.getTime() + authProperties.getLoginExpiredSeconds() * 2L * 1000L));
        refreshToken.setStatus(MemberRefreshTokenStatus.ACTIVE);
        refreshToken.setCreateDate(now);
        memberRefreshTokenDao.insert(refreshToken);

        MemberTokenResult result = new MemberTokenResult();
        result.setMemberId(member.getId());
        result.setAccessToken(accessTokenValue);
        result.setRefreshToken(refreshTokenValue);
        result.setExpiresIn(session.remainingSeconds(now));
        return result;
    }

    private Member requireActiveMember(EntityId memberId) throws ApiException {
        Member member = memberService.getById(memberId);
        if (member == null || !member.isActive()) {
            throw new ApiException("会员状态不可用");
        }
        return member;
    }

    private MemberIdentity requireIdentity(MemberIdentityType type, String value) throws ApiException {
        MemberIdentity identity = memberIdentityService.getByIdentity(type, value);
        if (identity == null || !identity.isEnabled()) {
            throw new ApiException("用户名或密码错误");
        }
        return identity;
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

    private void validateSmsCode(String loginToken, String mobile, String validateCode) throws ApiException {
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

    private MemberLoginForm getLoginForm(String loginToken) throws ApiException {
        MemberLoginForm form = memberLoginFormDao.getByToken(loginToken);
        if (form == null || !form.validateCheckCode()) {
            throw new ApiException("登录表单已失效");
        }
        return form;
    }

    private String decryptPassword(String loginToken, String encryptedPassword) throws ApiException {
        MemberLoginForm form = getLoginForm(loginToken);
        String[] parts = StringUtils.split(form.getPrivateKey(), PRIVATE_KEY_SEPARATOR);
        if (parts == null || parts.length != 2) {
            throw new ApiException("登录表单密钥已失效");
        }
        return RSAUtils.decryptBase64(encryptedPassword, new RSAUtils.ReadableKeyPair(null, parts[0], null, parts[1]));
    }

    private String privateKeyValue(RSAUtils.ReadableKeyPair keyPair) {
        return keyPair.getModulus() + PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent();
    }

    private String tokenHash(String token) {
        return Sha256Helper.hashBase64Url(token);
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
