package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.dao.MemberAuthSessionDao;
import com.github.thundax.modules.auth.dao.MemberAuthSessionRuntimeDao;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalRefreshTokenDao;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
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

    private final AuthProperties authProperties;
    private final MemberService memberService;
    private final PrincipalAuthService principalAuthService;
    private final MemberAuthSessionDao memberAuthSessionDao;
    private final MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao;
    private final PrincipalAccessTokenDao principalAccessTokenDao;
    private final PrincipalRefreshTokenDao principalRefreshTokenDao;

    public MemberAuthServiceImpl(
            AuthProperties authProperties,
            MemberService memberService,
            PrincipalAuthService principalAuthService,
            MemberAuthSessionDao memberAuthSessionDao,
            MemberAuthSessionRuntimeDao memberAuthSessionRuntimeDao,
            PrincipalAccessTokenDao principalAccessTokenDao,
            PrincipalRefreshTokenDao principalRefreshTokenDao) {
        this.authProperties = authProperties;
        this.memberService = memberService;
        this.principalAuthService = principalAuthService;
        this.memberAuthSessionDao = memberAuthSessionDao;
        this.memberAuthSessionRuntimeDao = memberAuthSessionRuntimeDao;
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
        return createTokenResult(member, principalIdentity, "ACCOUNT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(String mobile) throws ApiException {
        PrincipalIdentity identity = requireIdentity(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        return createTokenResult(requireActiveMember(identity.getPrincipalKey().getPrincipalId()), identity, "SMS");
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
        MemberAuthSession session = memberAuthSessionDao.getById(authSessionId(oldRefreshToken.getSessionId()));
        return createTokenResult(member, session, "REFRESH");
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
        MemberAuthSession session = memberAuthSessionDao.getById(authSessionId(token.getSessionId()));
        if (session != null) {
            session.logout(now);
            memberAuthSessionDao.update(session);
            memberAuthSessionRuntimeDao.deleteById(session.getId());
        }
    }

    @Override
    public PrincipalAccessToken getValidAccessToken(String accessToken) {
        PrincipalAccessToken token = principalAccessTokenDao.getByToken(accessToken);
        return token != null && token.canAccess(new Date()) ? token : null;
    }

    private MemberTokenResult createTokenResult(Member member, PrincipalIdentity identity, String loginType) {
        MemberAuthSession session = new MemberAuthSession();
        session.setPrincipalKey(PrincipalKey.of(PrincipalType.MEMBER, member.getId()));
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
        if (session.getId() == null) {
            session.setId(memberAuthSessionDao.insert(session));
        }
        memberAuthSessionRuntimeDao.insert(session, session.remainingSeconds(now));
        String authSessionId = EntityIdCodec.toStringValue(session.getId());
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.MEMBER, member.getId());

        String accessTokenValue = UuidHelper.compact();
        PrincipalAccessToken accessToken = new PrincipalAccessToken();
        accessToken.setTokenCode(PrincipalAccessTokenCode.of(UuidHelper.compact()));
        accessToken.setClientId(MEMBER_CLIENT_ID);
        accessToken.setSessionId(PrincipalAuthSessionId.of(authSessionId));
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
        refreshToken.setSessionId(PrincipalAuthSessionId.of(authSessionId));
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

    private EntityId authSessionId(PrincipalAuthSessionId sessionId) {
        return EntityIdCodec.toDomain(Long.valueOf(sessionId.value()));
    }
}
