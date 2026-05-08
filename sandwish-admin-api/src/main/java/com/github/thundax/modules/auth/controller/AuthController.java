package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
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
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
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

    private final AdminAuthService authService;

    @Autowired
    public AuthController(AdminAuthService authService) {

        this.authService = authService;
    }

    @ApiOperation(value = "请求预认证会话")
    @PostMapping(value = "form")
    @SysLogger("请求预认证会话")
    public AuthLoginFormResponse preAuthSession() throws ApiException {
        return AuthInterfaceAssembler.toLoginFormResponse(authService.createPreAuthSession());
    }

    @ApiOperation(value = "刷新预认证会话")
    @PostMapping(value = "form/refresh")
    @SysLogger("刷新预认证会话")
    public AuthLoginFormResponse refreshPreAuthSession(@Valid @RequestBody AuthLoginFormRefreshRequest request)
            throws ApiException {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw new InvalidParameterException("refreshToken");
        }

        return AuthInterfaceAssembler.toLoginFormResponse(authService.refreshPreAuthSession(request.getRefreshToken()));
    }

    @ApiOperation(value = "用户/密码登录")
    @PostMapping(value = "login")
    @SysLogger("用户/密码登录")
    public AuthAccessTokenResponse login(@Valid @RequestBody AuthLoginRequest request) throws ApiException {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (!authService.validateCaptcha(request.getLoginToken(), request.getCaptcha())) {
            // 刷新验证码
            authService.createCaptcha(request.getLoginToken());
            writeLog(currentRequest, "验证码失败", request);
            throw new InvalidCaptchaException();
        }
        // 刷新验证码
        authService.createCaptcha(request.getLoginToken());

        String privateKey = authService.getPrivateKey(request.getLoginToken());
        // 解密密码（数据需要加密传输）
        String password = Sm2Helper.decrypt(request.getPassword(), privateKey);

        User user;
        try {
            user = authService.authenticatePassword(request.getUsername(), password);
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

        authService.releasePreAuthSession(request.getLoginToken());

        AccessToken accessToken = authService.getByUserId(EntityIdCodec.toStringValue(user.getId()));
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }

        return loginSuccess(user, request.getUsername(), "用户/密码登录成功");
    }

    @ApiOperation(value = "短信登录")
    @PostMapping(value = "login/sms")
    public AuthAccessTokenResponse loginBySms(@Valid @RequestBody SmsLoginRequest request) throws ApiException {
        User user =
                authService.authenticateSms(request.getLoginToken(), request.getMobile(), request.getValidateCode());
        return loginSuccess(user, request.getMobile(), "短信登录成功");
    }

    @ApiOperation(value = "企业微信登录")
    @PostMapping(value = "login/wecom")
    public AuthAccessTokenResponse loginByWecom(@Valid @RequestBody WecomLoginRequest request) throws ApiException {
        User user = authService.authenticateWecom(request.getCode());
        return loginSuccess(user, "wecom", "企业微信登录成功");
    }

    @ApiOperation(value = "GitHub 登录")
    @PostMapping(value = "login/github")
    public AuthAccessTokenResponse loginByGithub(@Valid @RequestBody GithubLoginRequest request) throws ApiException {
        User user = authService.authenticateGithub(request.getCode());
        return loginSuccess(user, "github", "GitHub登录成功");
    }

    @ApiOperation(value = "登出")
    @PostMapping(value = "logout")
    @SysLogger("登出")
    public Boolean logout(@Valid @RequestBody AuthLogoutRequest request) throws ApiException {
        if (StringUtils.isEmpty(request.getToken())) {
            throw new InvalidTokenException();
        }

        AccessToken accessToken = authService.getAccessToken(request.getToken());
        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        if (AuthUtils.validateCheckCode(accessToken.getCheckCode())) {
            throw new PermissionDeniedException();
        }

        authService.deleteAccessToken(accessToken);

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
        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.refreshAccessToken(request.getClientId(), request.getRefreshToken()));
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
                request.isApproved()));
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
                request.getRefreshToken()));
    }

    @ApiOperation(value = "OAuth2 撤销令牌")
    @PostMapping(value = "oauth2/revoke")
    public Boolean revoke(@Valid @RequestBody OAuth2TokenRequest request) throws ApiException {
        return authService.revokeOAuth2Token(request.getClientId(), request.getClientSecret(), request.getToken());
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

    private AuthAccessTokenResponse loginSuccess(User user, String loginName, String logTitle) {
        AccessToken accessToken = authService.getByUserId(EntityIdCodec.toStringValue(user.getId()));
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        writeLog(currentRequest, logTitle, user, loginName);
        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.createAccessToken(EntityIdCodec.toStringValue(user.getId()), loginName));
    }
}
