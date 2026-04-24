package com.github.thundax.modules.auth.api;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.api.querry.UsernameLoginQueryParam;
import com.github.thundax.modules.auth.api.vo.AccessTokenVo;
import com.github.thundax.modules.auth.api.vo.LoginFormVo;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wdit
 */
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
    LoginFormVo loginForm() throws ApiException;


    /**
     * 刷新登录令牌
     *
     * @param form 登录表单
     * @return 登录表单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "刷新登录令牌", notes = "ignore")
    @PostMapping(value = "form/refresh")
    @SysLogger("刷新登录令牌")
    LoginFormVo refreshLoginForm(@RequestBody LoginFormVo form) throws ApiException;


    /**
     * 用户/密码登录
     *
     * @param queryParam 用户名/密码登录参数
     * @return TOKEN
     * @throws ApiException API异常
     */
    @ApiOperation(value = "用户/密码登录", notes = "ignore")
    @PostMapping(value = "login")
    @SysLogger("用户/密码登录")
    AccessTokenVo login(@RequestBody UsernameLoginQueryParam queryParam) throws ApiException;


    /**
     * 登出
     *
     * @param token TOKEN
     * @return 成功:true; 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "登出", notes = "ignore")
    @PostMapping(value = "logout")
    @SysLogger("登出")
    Boolean logout(@RequestBody AccessTokenVo token) throws ApiException;

}
