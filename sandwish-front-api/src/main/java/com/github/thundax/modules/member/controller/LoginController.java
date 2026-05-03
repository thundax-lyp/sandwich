package com.github.thundax.modules.member.controller;

import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
