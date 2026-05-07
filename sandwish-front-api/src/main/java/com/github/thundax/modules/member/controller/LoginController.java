package com.github.thundax.modules.member.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.request.MemberAccountLoginRequest;
import com.github.thundax.modules.member.controller.request.MemberLogoutRequest;
import com.github.thundax.modules.member.controller.request.MemberRefreshTokenRequest;
import com.github.thundax.modules.member.controller.request.MemberSmsLoginRequest;
import com.github.thundax.modules.member.controller.response.MemberLoginFormResponse;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.controller.response.MemberTokenResponse;
import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import com.github.thundax.modules.member.service.MemberAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员认证")
@RestController
@RequestMapping(value = "/auth")
@PublicApi
public class LoginController {

    private final MemberAuthService memberAuthService;

    public LoginController(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @ApiOperation(value = "请求登录表单")
    @PostMapping("form")
    public MemberLoginFormResponse loginForm() throws ApiException {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(memberAuthService.createLoginForm());
    }

    @ApiOperation(value = "刷新登录表单")
    @PostMapping("form/refresh")
    public MemberLoginFormResponse refreshLoginForm(@Valid @RequestBody MemberRefreshTokenRequest request)
            throws ApiException {
        return MemberLoginInterfaceAssembler.toLoginFormResponse(
                memberAuthService.refreshLoginForm(request.getRefreshToken()));
    }

    @ApiOperation(value = "账号密码登录")
    @PostMapping("login")
    public MemberTokenResponse loginAccount(@Valid @RequestBody MemberAccountLoginRequest request) throws ApiException {
        return MemberLoginInterfaceAssembler.toTokenResponse(memberAuthService.loginAccount(
                request.getLoginToken(), request.getAccount(), request.getPassword(), request.getCaptcha()));
    }

    @ApiOperation(value = "短信登录")
    @PostMapping("login/sms")
    public MemberTokenResponse loginSms(@Valid @RequestBody MemberSmsLoginRequest request) throws ApiException {
        return MemberLoginInterfaceAssembler.toTokenResponse(
                memberAuthService.loginSms(request.getLoginToken(), request.getMobile(), request.getValidateCode()));
    }

    @ApiOperation(value = "刷新 access token")
    @PostMapping("token/refresh")
    public MemberTokenResponse refreshAccessToken(@Valid @RequestBody MemberRefreshTokenRequest request)
            throws ApiException {
        return MemberLoginInterfaceAssembler.toTokenResponse(
                memberAuthService.refreshAccessToken(request.getRefreshToken()));
    }

    @ApiOperation(value = "登录状态")
    @GetMapping("login")
    public MemberLoginStatusResponse login() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @ApiOperation(value = "检查登录状态")
    @GetMapping("check-login")
    public MemberLoginStatusResponse checkLogin() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @ApiOperation(value = "登出")
    @PostMapping("logout")
    public MemberLoginStatusResponse logout(@Valid @RequestBody MemberLogoutRequest request) throws ApiException {
        memberAuthService.logout(request.getAccessToken());
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }
}
