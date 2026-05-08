package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.modules.auth.codec.PrincipalAuthSessionIdCodec;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalAuthSessionDao;
import com.github.thundax.modules.auth.dao.PrincipalRefreshTokenDao;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenCode;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
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

    private static final String MEMBER_CLIENT_ID = "member-api";
    private final SnowflakeIdGenerator sessionIdGenerator = new SnowflakeIdGenerator();

    private final AuthProperties authProperties;
    private final MemberService memberService;
    private final PrincipalAuthService principalAuthService;
    private final PrincipalAuthSessionDao principalAuthSessionDao;
    private final PrincipalAccessTokenDao principalAccessTokenDao;
    private final PrincipalRefreshTokenDao principalRefreshTokenDao;

    public MemberAuthServiceImpl(
            AuthProperties authProperties,
            MemberService memberService,
            PrincipalAuthService principalAuthService,
            PrincipalAuthSessionDao principalAuthSessionDao,
            PrincipalAccessTokenDao principalAccessTokenDao,
            PrincipalRefreshTokenDao principalRefreshTokenDao) {
        this.authProperties = authProperties;
        this.memberService = memberService;
        this.principalAuthService = principalAuthService;
        this.principalAuthSessionDao = principalAuthSessionDao;
        this.principalAccessTokenDao = principalAccessTokenDao;
        this.principalRefreshTokenDao = principalRefreshTokenDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginAccount(String account, String plainPassword) throws ApiException {
        PrincipalIdentity principalIdentity;
        try {
            principalIdentity = principalAuthService.authenticatePassword(
                    PrincipalIdentityType.MEMBER_ACCOUNT,
                    account,
                    PrincipalCredentialType.MEMBER_PASSWORD,
                    plainPassword,
                    PrincipalPasswordPolicyDTO.disabled());
        } catch (InvalidPasswordException e) {
            throw new ApiException("用户名或密码错误");
        }
        Member member = requireActiveMember(principalIdentity.getPrincipalKey().getPrincipalId());
        return createTokenResult(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(String mobile) throws ApiException {
        PrincipalIdentity identity = requireIdentity(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        return createTokenResult(requireActiveMember(identity.getPrincipalKey().getPrincipalId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult refreshAccessToken(String refreshToken) throws ApiException {
        PrincipalRefreshToken oldRefreshToken = principalRefreshTokenDao.getByToken(refreshToken);
        Date now = new Date();
        if (oldRefreshToken == null || !oldRefreshToken.canRefresh(now)) {
            throw new ApiException("refreshToken已失效");
        }
        oldRefreshToken.markUsed();
        principalRefreshTokenDao.updateStatus(oldRefreshToken);
        Member member = requireActiveMember(oldRefreshToken.getPrincipalKey().getPrincipalId());
        PrincipalAuthSession session = principalAuthSessionDao.getById(oldRefreshToken.getSessionId());
        if (session == null || session.isExpired(now)) {
            throw new ApiException("refreshToken已失效");
        }
        return createTokenResult(member, session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String accessToken) throws ApiException {
        PrincipalAccessToken token = principalAccessTokenDao.getByToken(accessToken);
        if (token == null) {
            throw new ApiException("accessToken已失效");
        }
        Date now = new Date();
        token.revoke();
        principalAccessTokenDao.updateStatus(token);
        principalAuthSessionDao.deleteById(token.getSessionId());
    }

    @Override
    public PrincipalAccessToken getValidAccessToken(String accessToken) {
        PrincipalAccessToken token = principalAccessTokenDao.getByToken(accessToken);
        Date now = new Date();
        if (token == null || !token.canAccess(now)) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionDao.getById(token.getSessionId());
        if (session == null || session.isExpired(now)) {
            return null;
        }
        principalAuthSessionDao.touch(session.getId(), now, session.remainingSeconds(now));
        return token;
    }

    private MemberTokenResult createTokenResult(Member member) {
        Date now = new Date();
        PrincipalAuthSession session = new PrincipalAuthSession();
        session.setId(PrincipalAuthSessionIdCodec.nextId(sessionIdGenerator));
        session.setPrincipalKey(PrincipalKey.of(PrincipalType.MEMBER, member.getId()));
        session.setClientId(MEMBER_CLIENT_ID);
        session.setIssuedAt(now);
        session.setLastAccessTime(now);
        session.setExpireAt(new Date(now.getTime() + authProperties.getLoginExpiredSeconds() * 1000L));
        return createTokenResult(member, session);
    }

    private MemberTokenResult createTokenResult(Member member, PrincipalAuthSession session) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + authProperties.getLoginExpiredSeconds() * 1000L);
        session.setIssuedAt(now);
        session.setLastAccessTime(now);
        session.setExpireAt(expireAt);
        principalAuthSessionDao.insert(session, session.remainingSeconds(now));
        PrincipalAuthSessionId authSessionId = session.getId();
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.MEMBER, member.getId());

        String accessTokenValue = UuidHelper.compact();
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setTokenCode(PrincipalAccessTokenCode.of(UuidHelper.compact()));
        accessToken.setClientId(MEMBER_CLIENT_ID);
        accessToken.setSessionId(authSessionId);
        accessToken.setPrincipalKey(principalKey);
        accessToken.setIssuedAt(now);
        accessToken.setExpireAt(expireAt);
        accessToken.setStatus(PrincipalTokenStatus.ACTIVE);
        accessToken.setId(principalAccessTokenDao.insert(accessToken, accessTokenValue));

        String refreshTokenValue = UuidHelper.compact();
        PrincipalRefreshToken refreshToken = new PrincipalRefreshToken();
        refreshToken.setTokenCode(PrincipalRefreshTokenCode.of(UuidHelper.compact()));
        refreshToken.setAccessTokenId(accessToken.getId());
        refreshToken.setClientId(MEMBER_CLIENT_ID);
        refreshToken.setSessionId(authSessionId);
        refreshToken.setPrincipalKey(principalKey);
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(new Date(now.getTime() + authProperties.getLoginExpiredSeconds() * 2L * 1000L));
        refreshToken.setStatus(PrincipalTokenStatus.ACTIVE);
        principalRefreshTokenDao.insert(refreshToken, refreshTokenValue);

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
}
