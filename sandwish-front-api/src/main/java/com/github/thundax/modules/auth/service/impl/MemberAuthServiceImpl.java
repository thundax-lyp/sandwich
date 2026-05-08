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
import com.github.thundax.modules.auth.dao.MemberRefreshTokenDao;
import com.github.thundax.modules.auth.entity.MemberAccessToken;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.MemberRefreshToken;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberAuthServiceImpl implements MemberAuthService {

    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;
    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String MEMBER_PRIVATE_KEY_SEPARATOR = ":";

    private final AuthProperties authProperties;
    private final PreAuthSessionService preAuthSessionService;
    private final MemberService memberService;
    private final PrincipalAuthService principalAuthService;
    private final MemberAuthSessionDao memberAuthSessionDao;
    private final MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao;
    private final MemberAccessTokenDao memberAccessTokenDao;
    private final MemberRefreshTokenDao memberRefreshTokenDao;

    public MemberAuthServiceImpl(
            AuthProperties authProperties,
            PreAuthSessionService preAuthSessionService,
            MemberService memberService,
            PrincipalAuthService principalAuthService,
            MemberAuthSessionDao memberAuthSessionDao,
            MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao,
            MemberAccessTokenDao memberAccessTokenDao,
            MemberRefreshTokenDao memberRefreshTokenDao) {
        this.authProperties = authProperties;
        this.preAuthSessionService = preAuthSessionService;
        this.memberService = memberService;
        this.principalAuthService = principalAuthService;
        this.memberAuthSessionDao = memberAuthSessionDao;
        this.memberAuthSessionRuntimeDao = memberAuthSessionRuntimeDao;
        this.memberAccessTokenDao = memberAccessTokenDao;
        this.memberRefreshTokenDao = memberRefreshTokenDao;
    }

    @Override
    public PreAuthSession createPreAuthSession() throws ApiException {
        if (preAuthSessionService.count() > authProperties.getMaxLoginCount()) {
            throw new ApiException("登录请求过多");
        }
        PreAuthSession session = preAuthSessionService.create(authProperties.getLoginExpiredSeconds());
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        RSAUtils.ReadableKeyPair keyPair = RSAUtils.generateKeyPair();
        preAuthSessionService.upsertValue(
                session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt());
        preAuthSessionService.upsertValue(
                session.getId(), PRIVATE_KEY_ITEM, memberPrivateKeyValue(keyPair), session.getExpiredAt());
        return preAuthSessionService.getById(session.getId());
    }

    @Override
    public PreAuthSession refreshPreAuthSession(String refreshToken) throws ApiException {
        PreAuthSessionId sessionId = requireSessionIdByRefreshToken(refreshToken);
        PreAuthSession session = preAuthSessionService.refresh(
                sessionId, authProperties.getLoginExpiredSeconds(), REFRESH_TOKEN_GRACE_SECONDS);
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    @Override
    public String createCaptcha(String loginToken) throws ApiException {
        String captcha = PreAuthCodeHelper.generateCaptcha();
        writeCaptcha(requireSessionIdByToken(loginToken), captcha);
        return captcha;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginAccount(String loginToken, String account, String encryptedPassword, String captcha)
            throws ApiException {
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateCaptcha(token, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        String password = decryptRsaValue(token, encryptedPassword);
        PrincipalIdentity principalIdentity;
        try {
            principalIdentity = principalAuthService.authenticatePassword(
                    PrincipalIdentityType.MEMBER_ACCOUNT,
                    account,
                    PrincipalCredentialType.MEMBER_PASSWORD,
                    password,
                    PrincipalPasswordPolicyDTO.disabled());
        } catch (InvalidPasswordException e) {
            throw new ApiException("用户名或密码错误");
        }
        Member member = requireActiveMember(principalIdentity.getPrincipalKey().getPrincipalId());
        preAuthSessionService.release(requireSessionIdByToken(loginToken));
        return createTokenResult(member, principalIdentity, "ACCOUNT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(String loginToken, String mobile, String validateCode) throws ApiException {
        PreAuthSessionToken token = PreAuthSessionToken.of(loginToken);
        if (!validateSmsValidateCode(token, mobile, validateCode)) {
            throw new ApiException("短信验证码错误");
        }
        PrincipalIdentity identity = requireIdentity(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        preAuthSessionService.release(requireSessionIdByToken(loginToken));
        return createTokenResult(requireActiveMember(identity.getPrincipalKey().getPrincipalId()), identity, "SMS");
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

    private MemberTokenResult createTokenResult(Member member, PrincipalIdentity identity, String loginType) {
        MemberAuthSession session = new MemberAuthSession();
        session.setSessionId(UuidHelper.compact());
        session.setMemberId(member.getId());
        session.setIdentityId(identity.getId());
        session.setIdentityType(identity.getType());
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

    private PrincipalIdentity requireIdentity(PrincipalIdentityType type, String value) throws ApiException {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticateIdentity(type, value);
        } catch (InvalidPasswordException e) {
            throw new ApiException("用户名或密码错误");
        }
        return identity;
    }

    private boolean validateCaptcha(PreAuthSessionToken token, String captcha) throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, preAuthSessionService.findValue(requireSessionId(token), CAPTCHA_ITEM));
    }

    private boolean validateSmsValidateCode(PreAuthSessionToken token, String mobile, String validateCode)
            throws ApiException {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionId(token);
        return StringUtils.equals(preAuthSessionService.findValue(sessionId, SMS_MOBILE_ITEM), mobile)
                && StringUtils.equals(preAuthSessionService.findValue(sessionId, SMS_VALIDATE_CODE_ITEM), validateCode);
    }

    private String decryptRsaValue(PreAuthSessionToken token, String encryptedValue) throws ApiException {
        String privateKey = preAuthSessionService.findValue(requireSessionId(token), PRIVATE_KEY_ITEM);
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

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) throws ApiException {
        preAuthSessionService.upsertValue(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L);
    }

    private String memberPrivateKeyValue(RSAUtils.ReadableKeyPair keyPair) {
        return keyPair.getModulus() + MEMBER_PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent();
    }

    private PreAuthSessionId requireSessionIdByToken(String token) throws ApiException {
        return requireSessionId(PreAuthSessionToken.of(token));
    }

    private PreAuthSessionId requireSessionId(PreAuthSessionToken token) throws ApiException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByToken(token);
        if (sessionId == null) {
            throw new ApiException("登录表单已失效");
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) throws ApiException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByRefreshToken(PreAuthSessionToken.of(refreshToken));
        if (sessionId == null) {
            throw new ApiException("登录表单已失效");
        }
        return sessionId;
    }

    private String tokenHash(String token) {
        return Sha256Helper.hashBase64Url(token);
    }
}
