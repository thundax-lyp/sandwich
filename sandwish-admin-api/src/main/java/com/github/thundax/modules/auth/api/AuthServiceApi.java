package com.github.thundax.modules.auth.api;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.request.AuthLoginFormRefreshRequest;
import com.github.thundax.modules.auth.request.AuthLoginRequest;
import com.github.thundax.modules.auth.request.AuthLogoutRequest;
import com.github.thundax.modules.auth.response.AuthAccessTokenResponse;
import com.github.thundax.modules.auth.response.AuthLoginFormResponse;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "01-01. 鉴权")
@RequestMapping(value = "/api/auth")
@SysLogger(module = {"系统", "登录"})
public interface AuthServiceApi {

    /**
     * 请求登录令牌
     *
     * @return 登录表单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "请求登录令牌", notes = "ignore")
    @PostMapping(value = "form")
    @SysLogger("请求登录令牌")
    AuthLoginFormResponse loginForm() throws ApiException;

    /**
     * 刷新登录令牌
     *
     * @param request 登录表单刷新请求
     * @return 登录表单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "刷新登录令牌", notes = "ignore")
    @PostMapping(value = "form/refresh")
    @SysLogger("刷新登录令牌")
    AuthLoginFormResponse refreshLoginForm(@RequestBody AuthLoginFormRefreshRequest request) throws ApiException;

    /**
     * 用户/密码登录
     *
     * @param request 用户名密码登录请求
     * @return TOKEN
     * @throws ApiException API异常
     */
    @ApiOperation(value = "用户/密码登录", notes = "ignore")
    @PostMapping(value = "login")
    @SysLogger("用户/密码登录")
    AuthAccessTokenResponse login(@RequestBody AuthLoginRequest request) throws ApiException;

    /**
     * 登出
     *
     * @param request 退出登录请求
     * @return 成功:true; 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "登出", notes = "ignore")
    @PostMapping(value = "logout")
    @SysLogger("登出")
    Boolean logout(@RequestBody AuthLogoutRequest request) throws ApiException;
}
