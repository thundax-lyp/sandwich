package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.auth.api.AuthServiceApi;
import com.github.thundax.modules.auth.assembler.AuthInterfaceAssembler;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.exception.BannedAccountException;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.request.AuthLoginFormRefreshRequest;
import com.github.thundax.modules.auth.request.AuthLoginRequest;
import com.github.thundax.modules.auth.request.AuthLogoutRequest;
import com.github.thundax.modules.auth.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.response.AuthLoginFormResponse;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.SysLogUtils;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author thundax */
@RestController
public class AuthApiController extends BaseApiController implements AuthServiceApi {

    private final AuthService authService;
    private final UserService userService;
    private final AuthInterfaceAssembler authInterfaceAssembler;

    @Autowired
    public AuthApiController(
            Validator validator,
            AuthService authService,
            UserService userService,
            AuthInterfaceAssembler authInterfaceAssembler) {
        super(validator);

        this.authService = authService;
        this.userService = userService;
        this.authInterfaceAssembler = authInterfaceAssembler;
    }

    @Override
    public AuthLoginFormResponse loginForm() throws ApiException {
        return authInterfaceAssembler.toLoginFormResponse(authService.createLoginForm());
    }

    @Override
    public AuthLoginFormResponse refreshLoginForm(@RequestBody AuthLoginFormRefreshRequest request)
            throws ApiException {
        if (StringUtils.isBlank(request.getRefreshToken())) {
            throw new InvalidParameterException("refreshToken");
        }

        return authInterfaceAssembler.toLoginFormResponse(
                authService.refreshLoginForm(request.getRefreshToken()));
    }

    @Override
    public AuthAccessTokenResponse login(@RequestBody AuthLoginRequest request)
            throws ApiException {
        validate(request);
        HttpServletRequest currentRequest = RequestUtils.currentRequest();
        if (!authService.validateCaptcha(request.getLoginToken(), request.getCaptcha())) {
            // 刷新验证码
            authService.createCaptcha(request.getLoginToken());
            writeLog(currentRequest, "验证码失败", request);
            throw new InvalidCaptchaException();
        }
        // 刷新验证码
        authService.createCaptcha(request.getLoginToken());

        User user = userService.getByLoginName(request.getUsername());
        if (user == null) {
            writeLog(currentRequest, "用户失败", request);
            throw new InvalidUsernamePasswordException();
        }

        if (!user.isEnable()) {
            writeLog(currentRequest, "用户失败", request);
            throw new BannedAccountException();
        }

        String privateKey = authService.getPrivateKey(request.getLoginToken());
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(request.getPassword(), privateKey);

        try {
            authService.validatePassword(user, password);
        } catch (ApiException e) {
            if (!(e instanceof InvalidUsernamePasswordException)
                    && user != null
                    && StringUtils.isNotBlank(user.getLoginName())) {
                if (e.getMessage() != null && e.getMessage().contains("锁定")) {
                    writeLog(currentRequest, "用户锁定", request);
                } else {
                    writeLog(currentRequest, "密码输入错误", request);
                }
            }
            throw e;
        }

        authService.deleteLoginForm(request.getLoginToken());

        AccessToken accessToken = authService.findByUserId(user.getId());
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }

        // 更新登录信息
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() == null ? 0 : user.getLoginCount() + 1);
        userService.updateLoginInfo(user);

        return authInterfaceAssembler.toAccessTokenResponse(
                authService.createAccessToken(user.getId()));
    }

    @Override
    public Boolean logout(@RequestBody AuthLogoutRequest request) throws ApiException {
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

    /** 记录登录日志 */
    private void writeLog(
            HttpServletRequest currentRequest, String title, AuthLoginRequest request) {
        Log log = new Log();
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(RequestUtils.getRemoteAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType("1");
        log.setRequestParams(authInterfaceAssembler.toLogJson(request));
        log.setSignable(true);
        SysLogUtils.saveLog(log);
    }
}
