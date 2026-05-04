package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
import com.github.thundax.modules.auth.controller.request.AuthLoginFormRefreshRequest;
import com.github.thundax.modules.auth.controller.request.AuthLoginRequest;
import com.github.thundax.modules.auth.controller.request.AuthLogoutRequest;
import com.github.thundax.modules.auth.controller.request.AuthTokenRequest;
import com.github.thundax.modules.auth.controller.request.GithubLoginRequest;
import com.github.thundax.modules.auth.controller.request.SmsLoginRequest;
import com.github.thundax.modules.auth.controller.request.TokenRefreshRequest;
import com.github.thundax.modules.auth.controller.request.WecomLoginRequest;
import com.github.thundax.modules.auth.controller.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.controller.response.AuthLoginFormResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2IntrospectionResponse;
import com.github.thundax.modules.auth.controller.response.OAuth2UserinfoResponse;
import com.github.thundax.modules.auth.controller.response.TokenVerifyResponse;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.service.UserService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Api(tags = "01-01. 鉴权")
@RequestMapping(value = "/api/auth")
@SysLogger(module = {"系统", "登录"})
@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {

        this.authService = authService;
        this.userService = userService;
    }

    @ApiOperation(value = "请求登录令牌", notes = "ignore")
    @PostMapping(value = "form")
    @SysLogger("请求登录令牌")
    public AuthLoginFormResponse loginForm() throws ApiException {
        return AuthInterfaceAssembler.toLoginFormResponse(authService.createLoginForm());
    }

    @ApiOperation(value = "刷新登录令牌", notes = "ignore")
    @PostMapping(value = "form/refresh")
    @SysLogger("刷新登录令牌")
    public AuthLoginFormResponse refreshLoginForm(@Valid @RequestBody AuthLoginFormRefreshRequest request)
            throws ApiException {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw new InvalidParameterException("refreshToken");
        }

        return AuthInterfaceAssembler.toLoginFormResponse(authService.refreshLoginForm(request.getRefreshToken()));
    }

    @ApiOperation(value = "用户/密码登录", notes = "ignore")
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

        authService.deleteLoginForm(request.getLoginToken());

        AccessToken accessToken = authService.getByUserId(EntityIdCodec.toValue(user.getId()));
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }

        // 更新登录信息
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() == null ? 0 : user.getLoginCount() + 1);
        userService.updateLoginInfo(user);

        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.createAccessToken(EntityIdCodec.toValue(user.getId()), request.getUsername()));
    }

    @ApiOperation(value = "短信登录", notes = "ignore")
    @PostMapping(value = "login/sms")
    public AuthAccessTokenResponse loginBySms(@Valid @RequestBody SmsLoginRequest request) throws ApiException {
        User user =
                authService.authenticateSms(request.getLoginToken(), request.getMobile(), request.getValidateCode());
        return loginSuccess(user, request.getMobile());
    }

    @ApiOperation(value = "企业微信登录", notes = "ignore")
    @PostMapping(value = "login/wecom")
    public AuthAccessTokenResponse loginByWecom(@Valid @RequestBody WecomLoginRequest request) throws ApiException {
        User user = authService.authenticateWecom(request.getCode());
        return loginSuccess(user, "wecom");
    }

    @ApiOperation(value = "GitHub 登录", notes = "ignore")
    @PostMapping(value = "login/github")
    public AuthAccessTokenResponse loginByGithub(@Valid @RequestBody GithubLoginRequest request) throws ApiException {
        User user = authService.authenticateGithub(request.getCode());
        return loginSuccess(user, "github");
    }

    @ApiOperation(value = "登出", notes = "ignore")
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

    @ApiOperation(value = "校验 token", notes = "ignore")
    @PostMapping(value = "token/verify")
    public TokenVerifyResponse verifyToken(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toTokenVerifyResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "OAuth2 token introspection", notes = "ignore")
    @PostMapping(value = "oauth2/introspect")
    public OAuth2IntrospectionResponse introspect(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toIntrospectionResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "OAuth2 userinfo", notes = "ignore")
    @PostMapping(value = "oauth2/userinfo")
    public OAuth2UserinfoResponse userinfo(@Valid @RequestBody AuthTokenRequest request) {
        return AuthInterfaceAssembler.toUserinfoResponse(authService.queryToken(request.getToken()));
    }

    @ApiOperation(value = "刷新 token", notes = "ignore")
    @PostMapping(value = "token/refresh")
    public AuthAccessTokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) throws ApiException {
        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.refreshAccessToken(request.getClientId(), request.getRefreshToken()));
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
        log.setSignable(true);
        SysLogUtils.saveLog(log);
    }

    private AuthAccessTokenResponse loginSuccess(User user, String loginName) {
        AccessToken accessToken = authService.getByUserId(EntityIdCodec.toValue(user.getId()));
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() == null ? 0 : user.getLoginCount() + 1);
        userService.updateLoginInfo(user);
        return AuthInterfaceAssembler.toAccessTokenResponse(
                authService.createAccessToken(EntityIdCodec.toValue(user.getId()), loginName));
    }
}
