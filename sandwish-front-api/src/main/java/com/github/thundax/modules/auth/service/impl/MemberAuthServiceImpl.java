package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.utils.encrypt.Sha256Helper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberAccessTokenDao;
import com.github.thundax.modules.auth.dao.MemberAuthSessionDao;
import com.github.thundax.modules.auth.dao.MemberAuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.MemberRefreshTokenDao;
import com.github.thundax.modules.auth.entity.MemberAccessToken;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.MemberRefreshToken;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.dto.PreAuthSessionDTO;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberAuthServiceImpl implements MemberAuthService {

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
    public PreAuthSessionDTO createPreAuthSession() throws ApiException {
        return preAuthSessionService.createPreAuthSession(PrincipalType.MEMBER);
    }

    @Override
    public PreAuthSessionDTO refreshPreAuthSession(String refreshToken) throws ApiException {
        return preAuthSessionService.refreshPreAuthSession(PrincipalType.MEMBER, refreshToken);
    }

    @Override
    public String createCaptcha(String loginToken) throws ApiException {
        return preAuthSessionService.createCaptcha(PrincipalType.MEMBER, loginToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginAccount(String loginToken, String account, String encryptedPassword, String captcha)
            throws ApiException {
        if (!preAuthSessionService.validateCaptcha(PrincipalType.MEMBER, loginToken, captcha)) {
            throw new ApiException("图形验证码错误");
        }
        String password = preAuthSessionService.decryptRsaValue(PrincipalType.MEMBER, loginToken, encryptedPassword);
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
        preAuthSessionService.releasePreAuthSession(PrincipalType.MEMBER, loginToken);
        return createTokenResult(member, principalIdentity, "ACCOUNT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(String loginToken, String mobile, String validateCode) throws ApiException {
        if (!preAuthSessionService.validateSmsValidateCode(PrincipalType.MEMBER, loginToken, mobile, validateCode)) {
            throw new ApiException("短信验证码错误");
        }
        PrincipalIdentity identity = requireIdentity(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        preAuthSessionService.releasePreAuthSession(PrincipalType.MEMBER, loginToken);
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

    private String tokenHash(String token) {
        return Sha256Helper.hashBase64Url(token);
    }
}
