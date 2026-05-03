package com.github.thundax.modules.member.controller;

import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
public class LoginController {

    @GetMapping("login")
    public MemberLoginStatusResponse login() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @GetMapping("check-login")
    public MemberLoginStatusResponse checkLogin() {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        return MemberLoginInterfaceAssembler.toLoginStatusResponse(principal);
    }

    @PostMapping("logout")
    public MemberLoginStatusResponse logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }
}
