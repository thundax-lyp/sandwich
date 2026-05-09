package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiController;
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
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.auth.service.result.AuthAccessTokenResult;
import com.github.thundax.modules.auth.utils.PreAuthCodeHelper;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.utils.SysLogUtils;
import com.github.thundax.modules.utils.IPUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
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
@RequestMapping(value = "/api/auth")
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

    @Autowired
    public AuthController(
            AdminAuthService authService, PreAuthSessionService preAuthSessionService, AuthProperties properties) {
        this.authService = authService;
        this.preAuthSessionService = preAuthSessionService;
        this.properties = properties;
    }

    @ApiOperation(value = "请求预认证会话")
    @PostMapping(value = "pre-auth-session")
    @SysLogger("请求预认证会话")
    public AuthLoginFormResponse preAuthSession() throws ApiException {
        return AuthInterfaceAssembler.toLoginFormResponse(createPreAuthSession());
    }

    @ApiOperation(value = "刷新预认证会话")
    @PostMapping(value = "pre-auth-session/refresh")
    @SysLogger("刷新预认证会话")
    public AuthLoginFormResponse refreshPreAuthSession(@Valid @RequestBody AuthLoginFormRefreshRequest request)
            throws ApiException {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw new InvalidParameterException("refreshToken");
        }

        return AuthInterfaceAssembler.toLoginFormResponse(refreshPreAuthSession(request.getRefreshToken()));
    }

    @ApiOperation(value = "用户/密码登录")
    @PostMapping(value = "login")
    @SysLogger("用户/密码登录")
    public AuthAccessTokenResponse login(@Valid @RequestBody AuthLoginRequest request) throws ApiException {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (!validateCaptcha(request.getLoginToken(), request.getCaptcha())) {
            createCaptcha(request.getLoginToken());
            writeLog(currentRequest, "验证码失败", request);
            authService.recordLoginFailed(
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip(currentRequest),
                    userAgent(currentRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID);
            throw new InvalidCaptchaException();
        }
        createCaptcha(request.getLoginToken());

        String privateKey = getPrivateKey(request.getLoginToken());
        String password = Sm2Helper.decrypt(request.getPassword(), privateKey);

        User user;
        try {
            user = authService.authenticatePassword(
                    request.getUsername(), password, ip(currentRequest), userAgent(currentRequest));
        } catch (ApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("锁定")) {
                writeLog(currentRequest, "用户锁定", request);
            } else if (e.getMessage() != null && e.getMessage().contains("密码输入错误")) {
                writeLog(currentRequest, "密码输入错误", request);
            } else if (!(e instanceof InvalidUsernamePasswordException)) {
                writeLog(currentRequest, "认证失败", request);
            } else {
                writeLog(currentRequest, "用户失败", request);
            }
            throw e;
        }

        releasePreAuthSession(request.getLoginToken());

        authService.deleteAccessTokensByUserId(EntityIdCodec.toStringValue(user.getId()));

        return loginSuccess(
                user,
                request.getUsername(),
                "用户/密码登录成功",
                PrincipalAuthenticationMethod.PASSWORD,
                PrincipalIdentityType.USER_ACCOUNT);
    }

    @ApiOperation(value = "短信登录")
    @PostMapping(value = "login/sms")
    public AuthAccessTokenResponse loginBySms(@Valid @RequestBody SmsLoginRequest request) throws ApiException {
        HttpServletRequest currentRequest = currentRequest();
        if (!validateSmsValidateCode(request.getLoginToken(), request.getMobile(), request.getValidateCode())) {
            authService.recordLoginFailed(
                    PrincipalAuthenticationMethod.SMS_CODE,
                    PrincipalIdentityType.USER_MOBILE,
                    ip(currentRequest),
                    userAgent(currentRequest),
                    PrincipalLoginEvent.REASON_CAPTCHA_INVALID);
            throw new InvalidCaptchaException();
        }
        User user = authService.authenticateSms(request.getMobile(), ip(currentRequest), userAgent(currentRequest));
        return loginSuccess(
                user,
                request.getMobile(),
                "短信登录成功",
                PrincipalAuthenticationMethod.SMS_CODE,
                PrincipalIdentityType.USER_MOBILE);
    }

    @ApiOperation(value = "企业微信登录")
    @PostMapping(value = "login/wecom")
    public AuthAccessTokenResponse loginByWecom(@Valid @RequestBody WecomLoginRequest request) throws ApiException {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateWecom(request.getCode(), ip(currentRequest), userAgent(currentRequest));
        return loginSuccess(
                user, "wecom", "企业微信登录成功", PrincipalAuthenticationMethod.WECOM, PrincipalIdentityType.USER_WECOM);
    }

    @ApiOperation(value = "GitHub 登录")
    @PostMapping(value = "login/github")
    public AuthAccessTokenResponse loginByGithub(@Valid @RequestBody GithubLoginRequest request) throws ApiException {
        HttpServletRequest currentRequest = currentRequest();
        User user = authService.authenticateGithub(request.getCode(), ip(currentRequest), userAgent(currentRequest));
        return loginSuccess(
                user, "github", "GitHub登录成功", PrincipalAuthenticationMethod.GITHUB, PrincipalIdentityType.USER_GITHUB);
    }

    @ApiOperation(value = "登出")
    @PostMapping(value = "logout")
    @SysLogger("登出")
    public Boolean logout(@Valid @RequestBody AuthLogoutRequest request) throws ApiException {
        if (StringUtils.isEmpty(request.getToken())) {
            throw new InvalidTokenException();
        }

        AuthAccessTokenResult accessToken = authService.getAccessToken(request.getToken());
        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        HttpServletRequest currentRequest = currentRequest();
        authService.deleteAccessToken(accessToken, ip(currentRequest), userAgent(currentRequest));

        return true;
    }

    @ApiOperation(value = "校验 token")
    @PostMapping(value = "token/verify")
    public TokenVerifyResponse verifyToken(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toTokenVerifyResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "OAuth2 token introspection")
    @PostMapping(value = "oauth2/introspect")
    public OAuth2IntrospectionResponse introspect(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toIntrospectionResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "OAuth2 userinfo")
    @PostMapping(value = "oauth2/userinfo")
    public OAuth2UserinfoResponse userinfo(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toUserinfoResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "刷新 token")
    @PostMapping(value = "token/refresh")
    public AuthAccessTokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) throws ApiException {
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.refreshAccessToken(
                request.getClientId(), request.getRefreshToken(), ip(currentRequest()), userAgent(currentRequest())));
    }

    @ApiOperation(value = "OAuth2 授权视图")
    @PostMapping(value = "oauth2/authorize")
    public OAuth2AuthorizationViewResponse authorize(@Valid @RequestBody OAuth2AuthorizeRequest request)
            throws ApiException {
        return AuthInterfaceAssembler.toAuthorizationViewResponse(authService.authorizeOAuth2(
                request.getClientId(), request.getRedirectUri(), request.getScopes(), request.getState()));
    }

    @ApiOperation(value = "OAuth2 授权决策")
    @PostMapping(value = "oauth2/decision")
    public OAuth2AuthorizationDecisionResponse decision(@Valid @RequestBody OAuth2DecisionRequest request)
            throws ApiException {
        return AuthInterfaceAssembler.toAuthorizationDecisionResponse(authService.decideOAuth2(
                request.getClientId(),
                request.getRedirectUri(),
                request.getScopes(),
                request.getState(),
                request.getCodeChallenge(),
                request.getCodeChallengeMethod(),
                request.getUserId(),
                request.isApproved(),
                ip(currentRequest()),
                userAgent(currentRequest())));
    }

    @ApiOperation(value = "OAuth2 授权码换 token")
    @PostMapping(value = "oauth2/token")
    public AuthAccessTokenResponse token(@Valid @RequestBody OAuth2TokenRequest request) throws ApiException {
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.exchangeOAuth2Token(
                request.getClientId(),
                request.getClientSecret(),
                request.getGrantType(),
                request.getRedirectUri(),
                request.getAuthorizationCode(),
                request.getCodeVerifier(),
                request.getRefreshToken(),
                ip(currentRequest()),
                userAgent(currentRequest())));
    }

    @ApiOperation(value = "OAuth2 撤销令牌")
    @PostMapping(value = "oauth2/revoke")
    public Boolean revoke(@Valid @RequestBody OAuth2TokenRequest request) throws ApiException {
        return authService.revokeOAuth2Token(request.getClientId(), request.getClientSecret(), request.getToken());
    }

    private PreAuthSession createPreAuthSession() throws ApiException {
        if (preAuthSessionService.count(new PreAuthSessionQuery()) > properties.getMaxLoginCount()) {
            throw new ApiException("登录请求过多");
        }
        PreAuthSession session =
                preAuthSessionService.create(new CreatePreAuthSessionCommand(properties.getLoginExpiredSeconds()));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        Sm2Helper.StringKeyPair keyPair = Sm2Helper.generateKeyPair();
        if (keyPair != null) {
            preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                    session.getId(), PUBLIC_KEY_ITEM, keyPair.getPublicKey(), session.getExpiredAt()));
            preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                    session.getId(), PRIVATE_KEY_ITEM, keyPair.getPrivateKey(), session.getExpiredAt()));
        }
        return preAuthSessionService.get(new PreAuthSessionQuery(session.getId(), null, null, null));
    }

    private PreAuthSession refreshPreAuthSession(String refreshToken) throws ApiException {
        PreAuthSession session = preAuthSessionService.refresh(new RefreshPreAuthSessionCommand(
                requireSessionIdByRefreshToken(refreshToken),
                properties.getLoginExpiredSeconds(),
                REFRESH_TOKEN_GRACE_SECONDS));
        writeCaptcha(session.getId(), PreAuthCodeHelper.generateCaptcha());
        return session;
    }

    private void releasePreAuthSession(String loginToken) {
        PreAuthSessionId sessionId =
                preAuthSessionService.getIdByToken(new PreAuthSessionQuery(null, PreAuthSessionToken.of(loginToken), null, null));
        if (sessionId != null) {
            preAuthSessionService.release(new ReleasePreAuthSessionCommand(sessionId));
        }
    }

    private String createCaptcha(String loginToken) throws InvalidTokenException {
        String captcha = PreAuthCodeHelper.generateCaptcha();
        writeCaptcha(requireSessionIdByToken(loginToken), captcha);
        return captcha;
    }

    private boolean validateCaptcha(String loginToken, String captcha) throws ApiException {
        if (StringUtils.isNotBlank(properties.getWhiteCaptcha())
                && StringUtils.equals(properties.getWhiteCaptcha(), captcha)) {
            return true;
        }
        return StringUtils.equals(captcha, getCaptcha(loginToken));
    }

    private String getCaptcha(String loginToken) throws ApiException {
        String captcha = preAuthSessionService.getValue(
                new PreAuthSessionQuery(requireSessionIdByToken(loginToken), null, null, CAPTCHA_ITEM));
        if (StringUtils.isEmpty(captcha)) {
            throw new InvalidCaptchaException();
        }
        return captcha;
    }

    private boolean validateSmsValidateCode(String loginToken, String mobile, String validateCode) throws ApiException {
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

    private String getPrivateKey(String loginToken) throws InvalidTokenException {
        String privateKey = preAuthSessionService.getValue(
                new PreAuthSessionQuery(requireSessionIdByToken(loginToken), null, null, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw new InvalidTokenException();
        }
        return privateKey;
    }

    private void writeCaptcha(PreAuthSessionId sessionId, String captcha) throws InvalidTokenException {
        preAuthSessionService.upsertValue(new UpsertPreAuthSessionValueCommand(
                sessionId, CAPTCHA_ITEM, captcha, System.currentTimeMillis() + CAPTCHA_EXPIRED_SECONDS * 1000L));
    }

    private PreAuthSessionId requireSessionIdByToken(String token) throws InvalidTokenException {
        PreAuthSessionId sessionId =
                preAuthSessionService.getIdByToken(new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null, null));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        return sessionId;
    }

    private PreAuthSessionId requireSessionIdByRefreshToken(String refreshToken) throws InvalidTokenException {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByRefreshToken(
                new PreAuthSessionQuery(null, null, PreAuthSessionToken.of(refreshToken), null));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        return sessionId;
    }

    private void writeLog(HttpServletRequest currentRequest, String title, AuthLoginRequest request) {
        Log log = new Log();
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(IPUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(AuthInterfaceAssembler.toLogJson(request));
        SysLogUtils.saveLog(log);
    }

    private void writeLog(HttpServletRequest currentRequest, String title, User user, String loginName) {
        Log log = new Log();
        log.setUserId(EntityIdCodec.toStringValue(user.getId()));
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(IPUtils.getIpAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType(LogType.ACCESS);
        log.setRequestParams(AuthInterfaceAssembler.toLogJson(loginName));
        SysLogUtils.saveLog(log);
    }

    private AuthAccessTokenResponse loginSuccess(
            User user,
            String loginName,
            String logTitle,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        authService.deleteAccessTokensByUserId(EntityIdCodec.toStringValue(user.getId()));
        HttpServletRequest currentRequest = currentRequest();
        writeLog(currentRequest, logTitle, user, loginName);
        return AuthInterfaceAssembler.toAccessTokenResponse(authService.createAccessToken(
                EntityIdCodec.toStringValue(user.getId()),
                loginName,
                ip(currentRequest),
                userAgent(currentRequest),
                authenticationMethod,
                identityType));
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private String ip(HttpServletRequest request) {
        return IPUtils.getIpAddr(request);
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }
}
