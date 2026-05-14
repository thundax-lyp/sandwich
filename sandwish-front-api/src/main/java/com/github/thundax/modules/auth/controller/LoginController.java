package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.crypto.RsaCrypto;
import com.github.thundax.common.exception.FrontResponseExceptions;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.web.util.RequestIpUtils;
import com.github.thundax.modules.auth.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.request.MemberAccountLoginRequest;
import com.github.thundax.modules.auth.controller.request.MemberLogoutRequest;
import com.github.thundax.modules.auth.controller.request.MemberRefreshTokenRequest;
import com.github.thundax.modules.auth.controller.request.MemberSmsLoginRequest;
import com.github.thundax.modules.auth.controller.response.MemberLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.auth.controller.response.MemberTokenResponse;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.security.CurrentMemberResolver;
import com.github.thundax.modules.auth.service.MemberAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.MemberAuthCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员认证")
@RestController
@RequestMapping(value = "/api/auth/session")
@PublicApi
public class LoginController {

    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final String MEMBER_PRIVATE_KEY_SEPARATOR = ":";
    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;

    private final MemberAuthService memberAuthService;
    private final PreAuthSessionService preAuthSessionService;
    private final AuthProperties authProperties;
    private final CurrentMemberResolver currentMemberResolver;

    public LoginController(
            MemberAuthService memberAuthService,
            PreAuthSessionService preAuthSessionService,
            AuthProperties authProperties,
            CurrentMemberResolver currentMemberResolver) {
        this.memberAuthService = memberAuthService;
        this.preAuthSessionService = preAuthSessionService;
        this.authProperties = authProperties;
        this.currentMemberResolver = currentMemberResolver;
    }

    @ApiOperation(value = "请求预认证会话")
    @PostMapping("pre-auth-session")
    public MemberLoginFormResponse preAuthSession() {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(createPreAuthSession());
    }

    @ApiOperation(value = "刷新预认证会话")
    @PostMapping("pre-auth-session/refresh")
    public MemberLoginFormResponse refreshPreAuthSession(@Valid @RequestBody MemberRefreshTokenRequest request) {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(refreshPreAuthSession(request.getRefreshToken()));
    }

    @ApiOperation(value = "账号密码登录")
    @PostMapping("login")
    public MemberTokenResponse loginAccount(
            @Valid @RequestBody MemberAccountLoginRequest request, HttpServletRequest httpRequest) {
        PreAuthSessionToken token = PreAuthSessionToken.of(request.getLoginToken());
        if (!validateCaptcha(token, request.getCaptcha())) {
            memberAuthService.recordLoginFailed(memberAuthCommand(
                    null,
                    null,
                    null,
                    null,
                    null,
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.MEMBER_ACCOUNT,
                    ip(httpRequest),
                    userAgent(httpRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw FrontResponseExceptions.invalidCaptcha();
        }
        String password = decryptRsaValue(token, request.getPassword());
        preAuthSessionService.release(
                new ReleasePreAuthSessionCommand(requireSessionIdByToken(request.getLoginToken())));
        return MemberLoginInterfaceAssembler.toTokenResponse(memberAuthService.loginAccount(memberAuthCommand(
                request.getAccount(),
                password,
                null,
                null,
                null,
                null,
                null,
                ip(httpRequest),
                userAgent(httpRequest),
                null)));
    }

    @ApiOperation(value = "短信登录")
    @PostMapping("login/sms")
    public MemberTokenResponse loginSms(
            @Valid @RequestBody MemberSmsLoginRequest request, HttpServletRequest httpRequest) {
        PreAuthSessionToken token = PreAuthSessionToken.of(request.getLoginToken());
        if (!validateSmsValidateCode(token, request.getMobile(), request.getValidateCode())) {
            memberAuthService.recordLoginFailed(memberAuthCommand(
                    null,
                    null,
                    null,
                    null,
                    null,
                    PrincipalAuthenticationMethod.SMS_CODE,
                    PrincipalIdentityType.MEMBER_MOBILE,
                    ip(httpRequest),
                    userAgent(httpRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw FrontResponseExceptions.invalidSmsCode();
        }
        preAuthSessionService.release(
                new ReleasePreAuthSessionCommand(requireSessionIdByToken(request.getLoginToken())));
        return MemberLoginInterfaceAssembler.toTokenResponse(memberAuthService.loginSms(memberAuthCommand(
                null,
                null,
                request.getMobile(),
                null,
                null,
                null,
                null,
                ip(httpRequest),
                userAgent(httpRequest),
                null)));
    }

    @ApiOperation(value = "刷新 access token")
    @PostMapping("token/refresh")
    public MemberTokenResponse refreshAccessToken(
            @Valid @RequestBody MemberRefreshTokenRequest request, HttpServletRequest httpRequest) {
        return MemberLoginInterfaceAssembler.toTokenResponse(memberAuthService.refreshAccessToken(memberAuthCommand(
                null,
                null,
                null,
                request.getRefreshToken(),
                null,
                null,
                null,
                ip(httpRequest),
                userAgent(httpRequest),
                null)));
    }

    @ApiOperation(value = "登录状态")
    @PostMapping("login/status")
    public MemberLoginStatusResponse login() {
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(currentMemberResolver.currentMemberId());
    }

    @ApiOperation(value = "检查登录状态")
    @PostMapping("check-login")
    public MemberLoginStatusResponse checkLogin() {
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(currentMemberResolver.currentMemberId());
    }

    @ApiOperation(value = "登出")
    @PostMapping("logout")
    public MemberLoginStatusResponse logout(
            @Valid @RequestBody MemberLogoutRequest request, HttpServletRequest httpRequest) {
        memberAuthService.logout(memberAuthCommand(
                null,
                null,
                null,
                null,
                request.getAccessToken(),
                null,
                null,
                ip(httpRequest),
                userAgent(httpRequest),
                null));
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }

    private MemberAuthCommand memberAuthCommand(
            String account,
            String plainPassword,
            String mobile,
            String refreshToken,
            String accessToken,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        MemberAuthCommand command = new MemberAuthCommand();
        command.setAccount(account);
        command.setPlainPassword(plainPassword);
        command.setMobile(mobile);
        command.setRefreshToken(refreshToken);
        command.setAccessToken(accessToken);
        command.setAuthenticationMethod(authenticationMethod);
        command.setIdentityType(identityType == null ? identityType(authenticationMethod) : identityType);
        command.setIp(ip);
        command.setUserAgent(userAgent);
        command.setReason(reason);
        return command;
    }

    private PrincipalIdentityType identityType(PrincipalAuthenticationMethod authenticationMethod) {
        if (authenticationMethod == PrincipalAuthenticationMethod.PASSWORD) {
            return PrincipalIdentityType.MEMBER_ACCOUNT;
        }
        if (authenticationMethod == PrincipalAuthenticationMethod.SMS_CODE) {
            return PrincipalIdentityType.MEMBER_MOBILE;
        }
        return null;
    }

    private PreAuthSession createPreAuthSession() {
        if (preAuthSessionService.countActiveSessions() > authProperties.getMaxLoginCount()) {
            throw FrontResponseExceptions.loginRequestTooMany();
        }
        PreAuthSession session =
                preAuthSessionService.create(new CreatePreAuthSessionCommand(authProperties.getLoginExpiredSeconds()));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        RsaCrypto.ReadableKeyPair keyPair = RsaCrypto.generateKeyPair();
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt()));
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                session.getId(),
                PRIVATE_KEY_ITEM,
                keyPair.getModulus() + MEMBER_PRIVATE_KEY_SEPARATOR + keyPair.getPrivateKeyExponent(),
                session.getExpiredAt()));
        return preAuthSessionService.get(session.getId());
    }

    private PreAuthSession refreshPreAuthSession(String refreshToken) {
        PreAuthSession session = preAuthSessionService.refresh(new RefreshPreAuthSessionCommand(
                requireSessionIdByRefreshToken(refreshToken),
                authProperties.getLoginExpiredSeconds(),
                REFRESH_TOKEN_GRACE_SECONDS));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    private boolean validateCaptcha(PreAuthSessionToken token, String captcha) {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(
                captcha,
                preAuthSessionService.getValue(new PreAuthSessionValueQuery(requireSessionId(token), CAPTCHA_ITEM)));
    }

    private boolean validateSmsValidateCode(PreAuthSessionToken token, String mobile, String validateCode) {
        if (StringUtils.isNotBlank(authProperties.getWhiteCaptcha())
                && StringUtils.equals(authProperties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionId(token);
        return StringUtils.equals(
                        preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, SMS_MOBILE_ITEM)),
                        mobile)
                && StringUtils.equals(
                        preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, SMS_VALIDATE_CODE_ITEM)),
                        validateCode);
    }

    private String decryptRsaValue(PreAuthSessionToken token, String encryptedValue) {
        String privateKey =
                preAuthSessionService.getValue(new PreAuthSessionValueQuery(requireSessionId(token), PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw FrontResponseExceptions.loginFormKeyExpired();
        }
        String[] privateKeyParts = StringUtils.split(privateKey, MEMBER_PRIVATE_KEY_SEPARATOR);
        if (privateKeyParts == null || privateKeyParts.length != 2) {
            throw FrontResponseExceptions.loginFormKeyExpired();
        }
        RsaCrypto.ReadableKeyPair keyPair =
                new RsaCrypto.ReadableKeyPair(null, privateKeyParts[0], null, privateKeyParts[1]);
        return RsaCrypto.decryptBase64(encryptedValue, keyPair);
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) {
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L));
    }

    private String ip(HttpServletRequest request) {
        return RequestIpUtils.getIpAddr(request);
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }

    private PreAuthSessionId requireSessionIdByToken(String token) {
        return requireSessionId(PreAuthSessionToken.of(token));
    }

    private PreAuthSessionId requireSessionId(PreAuthSessionToken token) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(token);
        if (sessionId == null) {
            throw FrontResponseExceptions.loginFormExpired();
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByRefreshToken(PreAuthSessionToken.of(refreshToken));
        if (sessionId == null) {
            throw FrontResponseExceptions.loginFormExpired();
        }
        return sessionId;
    }
}
