package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.exception.FrontBizExceptions;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.modules.auth.configure.AuthProperties;
import com.github.thundax.modules.auth.dao.PrincipalAccessTokenDao;
import com.github.thundax.modules.auth.dao.PrincipalAuthSessionDao;
import com.github.thundax.modules.auth.dao.PrincipalLoginEventDao;
import com.github.thundax.modules.auth.dao.PrincipalRefreshTokenDao;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.entity.PrincipalAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.PrincipalRefreshToken;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalLoginEventType;
import com.github.thundax.modules.auth.entity.enums.PrincipalTokenStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalRefreshTokenCode;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PrincipalAuthService;
import com.github.thundax.modules.auth.service.command.AuthenticateIdentityCommand;
import com.github.thundax.modules.auth.service.command.AuthenticatePasswordCommand;
import com.github.thundax.modules.auth.service.command.MemberAuthCommand;
import com.github.thundax.modules.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.github.thundax.modules.auth.service.query.MemberAuthQuery;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.valueobject.MemberIdCodec;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberAuthServiceImpl implements MemberAuthService {

    private static final String MEMBER_CLIENT_ID = "member-api";

    private final AuthProperties authProperties;
    private final MemberService memberService;
    private final PrincipalAuthService principalAuthService;
    private final PrincipalAuthSessionDao principalAuthSessionDao;
    private final PrincipalAccessTokenDao principalAccessTokenDao;
    private final PrincipalRefreshTokenDao principalRefreshTokenDao;

    @Autowired(required = false)
    private PrincipalLoginEventDao principalLoginEventDao;

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
    public MemberTokenResult loginAccount(MemberAuthCommand command) {
        return loginAccount(command.getAccount(), command.getPlainPassword(), command.getIp(), command.getUserAgent());
    }

    @Transactional(rollbackFor = Exception.class)
    private MemberTokenResult loginAccount(String account, String plainPassword, String ip, String userAgent) {
        PrincipalIdentity principalIdentity;
        try {
            principalIdentity = principalAuthService.authenticatePassword(new AuthenticatePasswordCommand(
                    PrincipalIdentityType.MEMBER_ACCOUNT,
                    account,
                    PrincipalCredentialType.MEMBER_PASSWORD,
                    plainPassword,
                    PrincipalPasswordPolicyDTO.disabled()));
        } catch (InvalidPasswordException e) {
            writeLoginEvent(
                    null,
                    PrincipalLoginEventType.LOGIN_FAILED,
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.MEMBER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_INVALID_CREDENTIAL);
            throw FrontBizExceptions.invalidUsernamePassword();
        }
        Member member = requireActiveMember(principalIdentity.getPrincipalKey().getPrincipalId());
        MemberTokenResult result = createTokenResult(member);
        writeLoginEvent(
                principalIdentity.getPrincipalKey(),
                PrincipalLoginEventType.LOGIN_SUCCESS,
                PrincipalAuthenticationMethod.PASSWORD,
                PrincipalIdentityType.MEMBER_ACCOUNT,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult loginSms(MemberAuthCommand command) {
        return loginSms(command.getMobile(), command.getIp(), command.getUserAgent());
    }

    @Transactional(rollbackFor = Exception.class)
    private MemberTokenResult loginSms(String mobile, String ip, String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = requireIdentity(PrincipalIdentityType.MEMBER_MOBILE, mobile);
        } catch (BizException e) {
            writeLoginEvent(
                    null,
                    PrincipalLoginEventType.LOGIN_FAILED,
                    PrincipalAuthenticationMethod.SMS_CODE,
                    PrincipalIdentityType.MEMBER_MOBILE,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_IDENTITY_NOT_FOUND);
            throw e;
        }
        MemberTokenResult result =
                createTokenResult(requireActiveMember(identity.getPrincipalKey().getPrincipalId()));
        writeLoginEvent(
                identity.getPrincipalKey(),
                PrincipalLoginEventType.LOGIN_SUCCESS,
                PrincipalAuthenticationMethod.SMS_CODE,
                PrincipalIdentityType.MEMBER_MOBILE,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberTokenResult refreshAccessToken(MemberAuthCommand command) {
        return refreshAccessToken(command.getRefreshToken(), command.getIp(), command.getUserAgent());
    }

    @Transactional(rollbackFor = Exception.class)
    private MemberTokenResult refreshAccessToken(String refreshToken, String ip, String userAgent) {
        PrincipalRefreshToken oldRefreshToken = principalRefreshTokenDao.getByToken(refreshToken);
        Date now = new Date();
        if (oldRefreshToken == null || !oldRefreshToken.canRefresh(now)) {
            throw FrontBizExceptions.refreshTokenExpired();
        }
        oldRefreshToken.markUsed();
        principalRefreshTokenDao.updateStatus(oldRefreshToken);
        Member member = requireActiveMember(oldRefreshToken.getPrincipalKey().getPrincipalId());
        PrincipalAuthSession session = principalAuthSessionDao.getById(oldRefreshToken.getSessionId());
        if (session == null || session.isExpired(now)) {
            throw FrontBizExceptions.refreshTokenExpired();
        }
        MemberTokenResult result = createTokenResult(member, session);
        writeLoginEvent(
                oldRefreshToken.getPrincipalKey(),
                PrincipalLoginEventType.TOKEN_REFRESH,
                PrincipalAuthenticationMethod.REFRESH_TOKEN,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(MemberAuthCommand command) {
        logout(command.getAccessToken(), command.getIp(), command.getUserAgent());
    }

    @Transactional(rollbackFor = Exception.class)
    private void logout(String accessToken, String ip, String userAgent) {
        PrincipalAccessToken token = principalAccessTokenDao.getByToken(accessToken);
        if (token == null) {
            throw FrontBizExceptions.accessTokenExpired();
        }
        Date now = new Date();
        token.revoke();
        principalAccessTokenDao.updateStatus(token);
        principalAuthSessionDao.deleteById(token.getSessionId());
        writeLoginEvent(
                token.getPrincipalKey(),
                PrincipalLoginEventType.LOGOUT,
                PrincipalAuthenticationMethod.PASSWORD,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_USER_LOGOUT);
    }

    @Override
    public PrincipalAccessToken getValidAccessToken(MemberAuthQuery query) {
        PrincipalAccessToken token = principalAccessTokenDao.getByToken(query.getAccessToken());
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

    @Override
    public void recordLoginFailed(MemberAuthCommand command) {
        writeLoginEvent(
                null,
                PrincipalLoginEventType.LOGIN_FAILED,
                command.getAuthenticationMethod(),
                command.getIdentityType(),
                command.getIp(),
                command.getUserAgent(),
                command.getReason());
    }

    private MemberTokenResult createTokenResult(Member member) {
        Date now = new Date();
        PrincipalAuthSession session = PrincipalAuthSession.create(
                PrincipalKey.of(PrincipalType.MEMBER, MemberIdCodec.toValue(member.getId())),
                MEMBER_CLIENT_ID,
                now,
                authProperties.getLoginExpiredSeconds());
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
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.MEMBER, MemberIdCodec.toValue(member.getId()));

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
        result.setMemberId(EntityId.of(MemberIdCodec.toValue(member.getId())));
        result.setAccessToken(accessTokenValue);
        result.setRefreshToken(refreshTokenValue);
        result.setExpiresIn(session.remainingSeconds(now));
        return result;
    }

    private Member requireActiveMember(Long memberId) {
        Member member = memberService.get(MemberIdCodec.toDomain(memberId));
        if (member == null || !member.isActive()) {
            throw FrontBizExceptions.memberUnavailable();
        }
        return member;
    }

    private PrincipalIdentity requireIdentity(PrincipalIdentityType type, String value) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticateIdentity(new AuthenticateIdentityCommand(type, value));
        } catch (InvalidPasswordException e) {
            throw FrontBizExceptions.invalidUsernamePassword();
        }
        return identity;
    }

    private void writeLoginEvent(
            PrincipalKey principalKey,
            PrincipalLoginEventType eventType,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        if (principalLoginEventDao == null) {
            return;
        }
        PrincipalLoginEvent event = new PrincipalLoginEvent();
        event.setPrincipalKey(principalKey);
        event.setClientId(MEMBER_CLIENT_ID);
        event.setEventType(eventType);
        event.setAuthenticationMethod(authenticationMethod);
        event.setIdentityType(identityType);
        event.setOccurredAt(new Date());
        event.setIp(ip);
        event.setUserAgent(userAgent);
        event.setReason(reason);
        principalLoginEventDao.insert(event);
    }
}
