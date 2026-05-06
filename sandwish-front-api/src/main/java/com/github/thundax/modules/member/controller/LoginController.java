package com.github.thundax.modules.member.controller;

import com.github.thundax.common.security.annotation.PublicApi;
import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "会员认证")
@RestController
@RequestMapping(value = "/auth")
@PublicApi
public class LoginController {

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
    public MemberLoginStatusResponse logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }
}
