package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.auth.api.AuthServiceApi;
import com.github.thundax.modules.auth.api.querry.UsernameLoginQueryParam;
import com.github.thundax.modules.auth.api.vo.AccessTokenVo;
import com.github.thundax.modules.auth.api.vo.LoginFormVo;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.entity.LoginForm;
import com.github.thundax.modules.auth.exception.BannedAccountException;
import com.github.thundax.modules.auth.exception.InvalidCaptchaException;
import com.github.thundax.modules.auth.exception.InvalidUsernamePasswordException;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.utils.AuthUtils;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.SysLogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.Date;

/**
 * @author thundax
 */
@RestController
public class AuthApiController extends BaseApiController implements AuthServiceApi {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthApiController(Validator validator,
                             AuthService authService,
                             UserService userService) {
        super(validator);

        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public LoginFormVo loginForm() throws ApiException {
        return entityToVo(authService.createLoginForm());
    }


    @Override
    public LoginFormVo refreshLoginForm(@RequestBody LoginFormVo form) throws ApiException {
        if (StringUtils.isBlank(form.getRefreshToken())) {
            throw new InvalidParameterException("refreshToken");
        }

        return entityToVo(authService.refreshLoginForm(form.getRefreshToken()));
    }


    @Override
    public AccessTokenVo login(@RequestBody UsernameLoginQueryParam queryParam) throws ApiException {
        validate(queryParam);
        HttpServletRequest currentRequest = RequestUtils.currentRequest();
        if (!authService.validateCaptcha(queryParam.getLoginToken(), queryParam.getCaptcha())) {
            //刷新验证码
            authService.createCaptcha(queryParam.getLoginToken());
            writeLog(currentRequest, "验证码失败", queryParam);
            throw new InvalidCaptchaException();
        }
        //刷新验证码
        authService.createCaptcha(queryParam.getLoginToken());

        User user = userService.getByLoginName(queryParam.getUsername());
        if (user == null) {
            writeLog(currentRequest, "用户失败", queryParam);
            throw new InvalidUsernamePasswordException();
        }

        if (!user.isEnable()) {
            writeLog(currentRequest, "用户失败", queryParam);
            throw new BannedAccountException();
        }

        String privateKey = authService.getPrivateKey(queryParam.getLoginToken());
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(queryParam.getPassword(), privateKey);

        try {
            authService.validatePassword(user, password);
        } catch (ApiException e) {
            if (!(e instanceof InvalidUsernamePasswordException)
                    && user != null
                    && StringUtils.isNotBlank(user.getLoginName())) {
                if (e.getMessage() != null && e.getMessage().contains("锁定")) {
                    writeLog(currentRequest, "用户锁定", queryParam);
                } else {
                    writeLog(currentRequest, "密码输入错误", queryParam);
                }
            }
            throw e;
        }

        authService.deleteLoginForm(queryParam.getLoginToken());

        AccessToken accessToken = authService.findByUserId(user.getId());
        if (accessToken != null) {
            authService.deleteAccessToken(accessToken);
        }

        // 更新登录信息
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() == null ? 0 : user.getLoginCount() + 1);
        userService.updateLoginInfo(user);

        return entityToVo(authService.createAccessToken(user.getId()));
    }


    @Override
    public Boolean logout(@RequestBody AccessTokenVo token) throws ApiException {
        if (StringUtils.isEmpty(token.getToken())) {
            throw new InvalidTokenException();
        }

        AccessToken accessToken = authService.getAccessToken(token.getToken());
        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        if (AuthUtils.validateCheckCode(accessToken.getCheckCode())) {
            throw new PermissionDeniedException();
        }

        authService.deleteAccessToken(accessToken);

        return true;
    }

    @NonNull
    private LoginFormVo entityToVo(LoginForm entity) {
        LoginFormVo vo = new LoginFormVo();
        vo.setLoginToken(entity.getLoginToken());
        vo.setRefreshToken(entity.getRefreshTokenList().get(0));
        vo.setExpireSeconds(entity.getExpiredSeconds());
        vo.setPublicKey(entity.getPublicKey());
        return vo;
    }

    @NonNull
    private AccessTokenVo entityToVo(AccessToken entity) {
        AccessTokenVo vo = new AccessTokenVo();
        if (entity != null) {
            vo.setToken(entity.getToken());
        }
        return vo;
    }

    /**
     * 记录登录日志
     *
     */
    private void writeLog(HttpServletRequest currentRequest, String title, UsernameLoginQueryParam queryParam) {
        Log log = new Log();
        log.setTitle("系统-登录-" + title);
        log.setLogDate(new Date());
        log.setRemoteAddr(RequestUtils.getRemoteAddr(currentRequest));
        log.setUserAgent(currentRequest.getHeader("user-agent"));
        log.setRequestUri(currentRequest.getRequestURI());
        log.setMethod(currentRequest.getMethod());
        log.setType("1");
        if (queryParam != null) {
            queryParam.setPassword("******");
            log.setRequestParams(JsonUtils.toJson(queryParam));
        }
        log.setSignable(true);
        SysLogUtils.saveLog(log);
    }
}
