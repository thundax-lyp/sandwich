package com.github.thundax.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.util.RequestIpUtils;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.controller.request.AuthLoginFormRefreshRequest;
import com.github.thundax.modules.auth.controller.request.AuthLoginRequest;
import com.github.thundax.modules.auth.controller.request.AuthLogoutRequest;
import com.github.thundax.modules.auth.controller.request.AuthTokenRequest;
import com.github.thundax.modules.auth.controller.request.GithubLoginRequest;
import com.github.thundax.modules.auth.controller.request.OAuth2AuthorizeRequest;
import com.github.thundax.modules.auth.controller.request.OAuth2DecisionRequest;
import com.github.thundax.modules.auth.controller.request.OAuth2TokenRequest;
import com.github.thundax.modules.auth.controller.request.SmsLoginRequest;
import com.github.thundax.modules.auth.controller.request.TokenRefreshRequest;
import com.github.thundax.modules.auth.controller.request.WecomLoginRequest;
import com.github.thundax.modules.auth.controller.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.controller.response.AuthLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2AuthorizationDecisionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2AuthorizationViewResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.controller.response.TokenVerifyResponse;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.AdminAuthCommand;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.AdminAuthQuery;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.utils.SysLogMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Api(tags = "鉴权")
@RequestMapping(value = "/api/auth/session")
@SysLogger(module = {"系统", "登录"})
@WrappedApiController
@PublicApi
public class AuthController {

    private static final String CAPTCHA_ITEM = "CAPTCHA";
    private static final String PUBLIC_KEY_ITEM = "publicKey";
    private static final String PRIVATE_KEY_ITEM = "privateKey";
    private static final String SMS_MOBILE_ITEM = "SMS_MOBILE";
    private static final String SMS_VALIDATE_CODE_ITEM = "SMS_VALIDATE_CODE";
    private static final int CAPTCHA_EXPIRED_SECONDS = 60;
    private static final int REFRESH_TOKEN_GRACE_SECONDS = 60;

    private final AdminAuthService authService;
    private final PreAuthSessionService preAuthSessionService;
    private final AuthProperties properties;
    private final SysLogMessageService sysLogMessageService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthController(
            AdminAuthService authService,
            PreAuthSessionService preAuthSessionService,
            AuthProperties properties,
            SysLogMessageService sysLogMessageService,
            ObjectMapper objectMapper) {
        this.authService = authService;
        this.preAuthSessionService = preAuthSessionService;
        this.properties = properties;
        this.sysLogMessageService = sysLogMessageService;
        this.objectMapper = objectMapper;
    }

    @ApiOperation(value = "请求预认证会话")
    @PostMapping(value = "pre-auth-session")
    @SysLogger("请求预认证会话")
    public AuthLoginFormResponse preAuthSession() {
        return AuthInterfaceAssembler.toLoginFormResponse(createPreAuthSession());
    }

    @ApiOperation(value = "刷新预认证会话")
    @PostMapping(value = "pre-auth-session/refresh")
    @SysLogger("刷新预认证会话")
    public AuthLoginFormResponse refreshPreAuthSession(@Valid @RequestBody AuthLoginFormRefreshRequest request) {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw AdminResponseExceptions.invalidParameter("refreshToken");
        }

        return AuthInterfaceAssembler.toLoginFormResponse(refreshPreAuthSession(request.getRefreshToken()));
    }

    @ApiOperation(value = "用户/密码登录")
    @PostMapping(value = "login")
    @SysLogger("用户/密码登录")
    public AuthAccessTokenResponse login(@Valid @RequestBody AuthLoginRequest request) {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (!validateCaptcha(request.getLoginToken(), request.getCaptcha())) {
            createCaptcha(request.getLoginToken());
            writeLog(currentRequest, "验证码失败", request);
            authService.recordLoginFailed(loginFailedCommand(
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip(currentRequest),
                    userAgent(currentRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw new InvalidCaptchaException();
        }
        createCaptcha(request.getLoginToken());

        String privateKey = getPrivateKey(request.getLoginToken());
        String password = Sm2Crypto.decrypt(request.getPassword(), privateKey);

        User user;
        try {
            user = authService.authenticatePassword(passwordCommand(request.getUsername(), password, currentRequest));
        } catch (SandwishException e) {
            if (e.getMessage() != null && e.getMessage().contains("锁定")) {
                writeLog(currentRequest, "用户锁定", request);
            } else if (e.getMessage() != null && e.getMessage().contains("密码输入错误")) {
                writeLog(currentRequest, "密码输入错误", request);
            } else if (!"AUTH-00002".equals(e.getCode())) {
                writeLog(currentRequest, "认证失败", request);
            } else {
                writeLog(currentRequest, "用户失败", request);
            }
            throw e;
        }

        releasePreAuthSession(request.getLoginToken());

        authService.deleteAccessTokensByUserId(userIdCommand(UserIdCodec.toStringValue(user.getId())));

        return loginSuccess(
                user,
                request.getUsername(),
                "用户/密码登录成功",
                PrincipalAuthenticationMethod.PASSWORD,
                PrincipalIdentityType.USER_ACCOUNT);
    }

    @ApiOperation(value = "短信登录")
    @PostMapping(value = "login/sms")
    public AuthAccessTokenResponse loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        if (!validateSmsValidateCode(request.getLoginToken(), request.getMobile(), request.getValidateCode())) {
            authService.recordLoginFailed(loginFailedCommand(
                    PrincipalAuthenticationMethod.SMS_CODE,
                    PrincipalIdentityType.USER_MOBILE,
                    ip(currentRequest),
                    userAgent(currentRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID));
            throw new InvalidCaptchaException();
        }
        User user = authService.authenticateSms(mobileCommand(request.getMobile(), currentRequest));
        return loginSuccess(
                user,
                request.getMobile(),
                "短信登录成功",
                PrincipalAuthenticationMethod.SMS_CODE,
                PrincipalIdentityType.USER_MOBILE);
    }

    @ApiOperation(value = "企业微信登录")
    @PostMapping(value = "login/wecom")
    public AuthAccessTokenResponse loginByWecom(@Valid @RequestBody WecomLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateWecom(codeCommand(request.getCode(), currentRequest));
        return loginSuccess(
                user, "wecom", "企业微信登录成功", PrincipalAuthenticationMethod.WECOM, PrincipalIdentityType.USER_WECOM);
    }

    @ApiOperation(value = "GitHub 登录")
    @PostMapping(value = "login/github")
    public AuthAccessTokenResponse loginByGithub(@Valid @RequestBody GithubLoginRequest request) {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateGithub(codeCommand(request.getCode(), currentRequest));
        return loginSuccess(
                user, "github", "GitHub登录成功", PrincipalAuthenticationMethod.GITHUB, PrincipalIdentityType.USER_GITHUB);
    }

    @ApiOperation(value = "登出")
    @PostMapping(value = "logout")
    @SysLogger("登出")
    public Boolean logout(@Valid @RequestBody AuthLogoutRequest request) {
        if (StringUtils.isEmpty(request.getToken())) {
            throw AdminResponseExceptions.invalidToken();
        }

        AuthAccessTokenResult accessToken = authService.getAccessToken(tokenQuery(request.getToken()));
        if (accessToken == null) {
            throw AdminResponseExceptions.invalidToken();
        }

        HttpServletRequest currentRequest = currentRequest();
        authService.deleteAccessToken(accessTokenCommand(accessToken, currentRequest));

        return true;
    }

    @ApiOperation(value = "校验 token")
    @PostMapping(value = "token/verify")
    public TokenVerifyResponse verifyToken(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toTokenVerifyResponse(authService.getTokenInfo(tokenQuery(request.getToken())));
    }

    @ApiOperation(value = "OAuth2 token introspection")
    @PostMapping(value = "oauth2/introspect")
    public OAuth2IntrospectionResponse introspect(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toIntrospectionResponse(authService.getTokenInfo(tokenQuery(request.getToken())));
    }

    @ApiOperation(value = "OAuth2 userinfo")
    @PostMapping(value = "oauth2/userinfo")
    public OAuth2UserinfoResponse userinfo(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toUserinfoResponse(authService.getTokenInfo(tokenQuery(request.getToken())));
    }

    @ApiOperation(value = "刷新 token")
    @PostMapping(value = "token/refresh")
    public AuthAccessTokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.refreshAccessToken(
                refreshTokenCommand(request.getClientId(), request.getRefreshToken(), currentRequest())));
    }

    @ApiOperation(value = "OAuth2 授权视图")
    @PostMapping(value = "oauth2/authorize")
    public OAuth2AuthorizationViewResponse authorize(@Valid @RequestBody OAuth2AuthorizeRequest request) {
        return AuthInterfaceAssembler.toAuthorizationViewResponse(authService.authorizeOAuth2(oauthCommand(
                request.getClientId(), request.getRedirectUri(), request.getScopes(), request.getState())));
    }

    @ApiOperation(value = "OAuth2 授权决策")
    @PostMapping(value = "oauth2/decision")
    public OAuth2AuthorizationDecisionResponse decision(@Valid @RequestBody OAuth2DecisionRequest request) {
        return AuthInterfaceAssembler.toAuthorizationDecisionResponse(
                authService.decideOAuth2(decisionCommand(request)));
    }

    @ApiOperation(value = "OAuth2 授权码换 token")
    @PostMapping(value = "oauth2/token")
    public AuthAccessTokenResponse token(@Valid @RequestBody OAuth2TokenRequest request) {
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.exchangeOAuth2Token(exchangeCommand(request)));
    }

    @ApiOperation(value = "OAuth2 撤销令牌")
    @PostMapping(value = "oauth2/revoke")
    public Boolean revoke(@Valid @RequestBody OAuth2TokenRequest request) {
        return authService.revokeOAuth2Token(revokeTokenCommand(request));
    }

    private PreAuthSession createPreAuthSession() {
        if (preAuthSessionService.count(new PreAuthSessionQuery()) > properties.getMaxLoginCount()) {
            throw AdminResponseExceptions.loginRequestTooMany();
        }
        PreAuthSession session =
                preAuthSessionService.create(new CreatePreAuthSessionCommand(properties.getLoginExpiredSeconds()));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        Sm2Crypto.StringKeyPair keyPair = Sm2Crypto.generateKeyPair();
        if (keyPair != null) {
            preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                    session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt()));
            preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                    session.getId(), PRIVATE_KEY_ITEM, keyPair.getPrivateKey(), session.getExpiredAt()));
        }
        return preAuthSessionService.get(new PreAuthSessionQuery(session.getId(), null, null, null));
    }

    private PreAuthSession refreshPreAuthSession(String refreshToken) {
        PreAuthSession session = preAuthSessionService.refresh(new RefreshPreAuthSessionCommand(
                requireSessionIdByRefreshToken(refreshToken),
                properties.getLoginExpiredSeconds(),
                REFRESH_TOKEN_GRACE_SECONDS));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    private void releasePreAuthSession(String loginToken) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(
                new PreAuthSessionQuery(null, PreAuthSessionToken.of(loginToken), null, null));
        if (sessionId != null) {
            preAuthSessionService.release(new ReleasePreAuthSessionCommand(sessionId));
        }
    }

    private String createCaptcha(String loginToken) {
        String captcha = PreAuthCodeHelper.generateCaptcha();
        writeCaptcha(requireSessionIdByToken(loginToken), captcha);
        return captcha;
    }

    private boolean validateCaptcha(String loginToken, String captcha) {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, getCaptcha(loginToken));
    }

    private String getCaptcha(String loginToken) {
        String captcha = preAuthSessionService.getValue(
                new PreAuthSessionQuery(requireSessionIdByToken(loginToken), null, null, CAPTCHA_ITEM));
        if (StringUtils.isEmpty(captcha)) {
            throw new InvalidCaptchaException();
        }
        return captcha;
    }

    private boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode) {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), validateCode)) {
            return true;
        }
        PreAuthSessionId sessionId = requireSessionIdByToken(loginToken);
        String savedMobile =
                preAuthSessionService.getValue(new PreAuthSessionQuery(sessionId, null, null, SMS_MOBILE_ITEM));
        String savedValidateCode =
                preAuthSessionService.getValue(new PreAuthSessionQuery(sessionId, null, null, SMS_VALIDATE_CODE_ITEM));
        if (StringUtils.isEmpty(savedMobile) || StringUtils.isEmpty(savedValidateCode)) {
            throw new InvalidCaptchaException();
        }
        return StringUtils.equals(savedMobile, mobile) && StringUtils.equals(savedValidateCode, validateCode);
    }

    private String getPrivateKey(String loginToken) {
        String privateKey = preAuthSessionService.getValue(
                new PreAuthSessionQuery(requireSessionIdByToken(loginToken), null, null, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) {
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L));
    }

    private PreAuthSessionId requireSessionIdByToken(String token) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(
                new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null, null));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByRefreshToken(
                new PreAuthSessionQuery(null, null, PreAuthSessionToken.of(refreshToken), null));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        return sessionId;
    }

    private void writeLog(HttpServletRequest currentRequest, String title, AuthLoginRequest request) {
        Log log = new Log();
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(RequestIpUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(toLogJson(request));
        sysLogMessageService.saveLog(log);
    }

    private void writeLog(HttpServletRequest currentRequest, String title, User user, String loginName) {
        Log log = new Log();
        log.setUserId(UserIdCodec.toStringValue(user.getId()));
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(RequestIpUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(toLogJson(loginName));
        sysLogMessageService.saveLog(log);
    }

    private String toLogJson(AuthLoginRequest request) {
        if (request == null) {
            return null;
        }
        AuthLoginRequest maskedRequest = new AuthLoginRequest();
        maskedRequest.setLoginToken(request.getLoginToken());
        maskedRequest.setUsername(request.getUsername());
        maskedRequest.setPassword("******");
        maskedRequest.setCaptcha(request.getCaptcha());
        return toJson(maskedRequest);
    }

    private String toLogJson(String loginName) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("loginName", loginName);
        return toJson(request);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private AuthAccessTokenResponse loginSuccess(
            User user,
            String loginName,
            String logTitle,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        authService.deleteAccessTokensByUserId(userIdCommand(UserIdCodec.toStringValue(user.getId())));
        HttpServletRequest currentRequest = currentRequest();
        writeLog(currentRequest, logTitle, user, loginName);
        AdminAuthCommand command =
                accessTokenCommand(UserIdCodec.toStringValue(user.getId()), loginName, currentRequest);
        command.setAuthenticationMethod(authenticationMethod);
        command.setIdentityType(identityType);
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.createAccessToken(command));
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private AdminAuthQuery tokenQuery(String token) {
        AdminAuthQuery query = new AdminAuthQuery();
        query.setToken(token);
        return query;
    }

    private AdminAuthCommand passwordCommand(String loginName, String plainPassword, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setLoginName(loginName);
        command.setPlainPassword(plainPassword);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand accessTokenCommand(String userId, String loginName, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setUserId(userId);
        command.setLoginName(loginName);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand userIdCommand(String userId) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setUserId(userId);
        return command;
    }

    private AdminAuthCommand mobileCommand(String mobile, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setMobile(mobile);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand codeCommand(String code, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setCode(code);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand accessTokenCommand(AuthAccessTokenResult accessToken, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setAccessToken(accessToken);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand refreshTokenCommand(String clientId, String refreshToken, HttpServletRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setRefreshToken(refreshToken);
        command.setIp(ip(request));
        command.setUserAgent(userAgent(request));
        return command;
    }

    private AdminAuthCommand oauthCommand(String clientId, String redirectUri, List<String> scopes, String state) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(clientId);
        command.setRedirectUri(redirectUri);
        command.setScopes(scopes);
        command.setState(state);
        return command;
    }

    private AdminAuthCommand decisionCommand(OAuth2DecisionRequest request) {
        AdminAuthCommand command =
                oauthCommand(request.getClientId(), request.getRedirectUri(), request.getScopes(), request.getState());
        command.setCodeChallenge(request.getCodeChallenge());
        command.setCodeChallengeMethod(request.getCodeChallengeMethod());
        command.setUserId(request.getUserId());
        command.setApproved(request.isApproved());
        command.setIp(ip(currentRequest()));
        command.setUserAgent(userAgent(currentRequest()));
        return command;
    }

    private AdminAuthCommand exchangeCommand(OAuth2TokenRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(request.getClientId());
        command.setClientSecret(request.getClientSecret());
        command.setGrantType(request.getGrantType());
        command.setRedirectUri(request.getRedirectUri());
        command.setAuthorizationCode(request.getAuthorizationCode());
        command.setCodeVerifier(request.getCodeVerifier());
        command.setRefreshToken(request.getRefreshToken());
        command.setIp(ip(currentRequest()));
        command.setUserAgent(userAgent(currentRequest()));
        return command;
    }

    private AdminAuthCommand revokeTokenCommand(OAuth2TokenRequest request) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setClientId(request.getClientId());
        command.setClientSecret(request.getClientSecret());
        command.setToken(request.getToken());
        return command;
    }

    private AdminAuthCommand loginFailedCommand(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        AdminAuthCommand command = new AdminAuthCommand();
        command.setAuthenticationMethod(authenticationMethod);
        command.setIdentityType(identityType);
        command.setIp(ip);
        command.setUserAgent(userAgent);
        command.setReason(reason);
        return command;
    }

    private String ip(HttpServletRequest request) {
        return RequestIpUtils.getIpAddr(request);
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }
}
