package com.github.thundax.modules.member.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.modules.member.assembler.MemberRegisterInterfaceAssembler;
import com.github.thundax.modules.member.controller.request.MemberAccountRegisterRequest;
import com.github.thundax.modules.member.controller.request.MemberEmailRegisterRequest;
import com.github.thundax.modules.member.controller.request.MemberMobileRegisterRequest;
import com.github.thundax.modules.member.controller.request.MemberRegisterEmailCodeRequest;
import com.github.thundax.modules.member.controller.request.MemberRegisterSmsCodeRequest;
import com.github.thundax.modules.member.controller.response.MemberRegisterResponse;
import com.github.thundax.modules.member.service.MemberRegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员注册")
@RequestMapping(value = "/auth/register")
@RestController
@PublicApi
public class RegisterController {

    private final MemberRegistrationService memberRegistrationService;

    public RegisterController(MemberRegistrationService memberRegistrationService) {
        this.memberRegistrationService = memberRegistrationService;
    }

    @ApiOperation(value = "账号密码注册")
    @PostMapping(value = "account")
    public MemberRegisterResponse registerAccount(@Valid @RequestBody MemberAccountRegisterRequest request)
            throws ApiException {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(memberRegistrationService.registerAccount(
                request.getLoginToken(),
                request.getName(),
                request.getAccount(),
                request.getPassword(),
                request.getCaptcha()));
    }

    @ApiOperation(value = "发送注册短信验证码")
    @PostMapping(value = "mobile/code")
    public MemberRegisterResponse sendSmsCode(@Valid @RequestBody MemberRegisterSmsCodeRequest request)
            throws ApiException {
        memberRegistrationService.sendRegisterSmsCode(
                request.getLoginToken(), request.getMobile(), request.getCaptcha());
        return MemberRegisterInterfaceAssembler.toCodeResponse();
    }

    @ApiOperation(value = "手机号注册")
    @PostMapping(value = "mobile")
    public MemberRegisterResponse registerMobile(@Valid @RequestBody MemberMobileRegisterRequest request)
            throws ApiException {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(memberRegistrationService.registerMobile(
                request.getLoginToken(), request.getName(), request.getMobile(), request.getValidateCode()));
    }

    @ApiOperation(value = "发送注册邮箱验证码")
    @PostMapping(value = "email/code")
    public MemberRegisterResponse sendEmailCode(@Valid @RequestBody MemberRegisterEmailCodeRequest request)
            throws ApiException {
        memberRegistrationService.sendRegisterEmailCode(
                request.getLoginToken(), request.getEmail(), request.getCaptcha());
        return MemberRegisterInterfaceAssembler.toCodeResponse();
    }

    @ApiOperation(value = "邮箱注册")
    @PostMapping(value = "email")
    public MemberRegisterResponse registerEmail(@Valid @RequestBody MemberEmailRegisterRequest request)
            throws ApiException {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(memberRegistrationService.registerEmail(
                request.getLoginToken(), request.getName(), request.getEmail(), request.getValidateCode()));
    }
}
