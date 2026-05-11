package com.github.thundax.modules.auth.controller;

import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.modules.auth.assembler.MemberRegisterInterfaceAssembler;
import com.github.thundax.modules.auth.controller.request.MemberAccountRegisterRequest;
import com.github.thundax.modules.auth.controller.request.MemberEmailRegisterRequest;
import com.github.thundax.modules.auth.controller.request.MemberMobileRegisterRequest;
import com.github.thundax.modules.auth.controller.request.MemberRegisterEmailCodeRequest;
import com.github.thundax.modules.auth.controller.request.MemberRegisterSmsCodeRequest;
import com.github.thundax.modules.auth.controller.response.MemberRegisterResponse;
import com.github.thundax.modules.auth.service.MemberRegistrationService;
import com.github.thundax.modules.auth.service.command.MemberRegistrationCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员注册")
@RequestMapping(value = "/api/auth/register")
@RestController
@PublicApi
public class RegisterController {

    private final MemberRegistrationService memberRegistrationService;

    public RegisterController(MemberRegistrationService memberRegistrationService) {
        this.memberRegistrationService = memberRegistrationService;
    }

    @ApiOperation(value = "账号密码注册")
    @PostMapping(value = "account")
    public MemberRegisterResponse registerAccount(@Valid @RequestBody MemberAccountRegisterRequest request) {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(
                memberRegistrationService.registerAccount(accountCommand(request)));
    }

    @ApiOperation(value = "发送注册短信验证码")
    @PostMapping(value = "mobile/code")
    public MemberRegisterResponse sendSmsCode(@Valid @RequestBody MemberRegisterSmsCodeRequest request) {
        memberRegistrationService.sendRegisterSmsCode(smsCodeCommand(request));
        return MemberRegisterInterfaceAssembler.toCodeResponse();
    }

    @ApiOperation(value = "手机号注册")
    @PostMapping(value = "mobile")
    public MemberRegisterResponse registerMobile(@Valid @RequestBody MemberMobileRegisterRequest request) {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(
                memberRegistrationService.registerMobile(mobileCommand(request)));
    }

    @ApiOperation(value = "发送注册邮箱验证码")
    @PostMapping(value = "email/code")
    public MemberRegisterResponse sendEmailCode(@Valid @RequestBody MemberRegisterEmailCodeRequest request) {
        memberRegistrationService.sendRegisterEmailCode(emailCodeCommand(request));
        return MemberRegisterInterfaceAssembler.toCodeResponse();
    }

    @ApiOperation(value = "邮箱注册")
    @PostMapping(value = "email")
    public MemberRegisterResponse registerEmail(@Valid @RequestBody MemberEmailRegisterRequest request) {
        return MemberRegisterInterfaceAssembler.toRegisterResponse(
                memberRegistrationService.registerEmail(emailCommand(request)));
    }

    private MemberRegistrationCommand accountCommand(MemberAccountRegisterRequest request) {
        MemberRegistrationCommand command = new MemberRegistrationCommand();
        command.setLoginToken(request.getLoginToken());
        command.setName(request.getName());
        command.setAccount(request.getAccount());
        command.setEncryptedPassword(request.getPassword());
        command.setCaptcha(request.getCaptcha());
        return command;
    }

    private MemberRegistrationCommand smsCodeCommand(MemberRegisterSmsCodeRequest request) {
        MemberRegistrationCommand command = new MemberRegistrationCommand();
        command.setLoginToken(request.getLoginToken());
        command.setMobile(request.getMobile());
        command.setCaptcha(request.getCaptcha());
        return command;
    }

    private MemberRegistrationCommand mobileCommand(MemberMobileRegisterRequest request) {
        MemberRegistrationCommand command = new MemberRegistrationCommand();
        command.setLoginToken(request.getLoginToken());
        command.setName(request.getName());
        command.setMobile(request.getMobile());
        command.setValidateCode(request.getValidateCode());
        return command;
    }

    private MemberRegistrationCommand emailCodeCommand(MemberRegisterEmailCodeRequest request) {
        MemberRegistrationCommand command = new MemberRegistrationCommand();
        command.setLoginToken(request.getLoginToken());
        command.setEmail(request.getEmail());
        command.setCaptcha(request.getCaptcha());
        return command;
    }

    private MemberRegistrationCommand emailCommand(MemberEmailRegisterRequest request) {
        MemberRegistrationCommand command = new MemberRegistrationCommand();
        command.setLoginToken(request.getLoginToken());
        command.setName(request.getName());
        command.setEmail(request.getEmail());
        command.setValidateCode(request.getValidateCode());
        return command;
    }
}
