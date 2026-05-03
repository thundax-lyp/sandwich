package com.github.thundax.modules.member.controller;

import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.controller.response.MemberLoginStatusResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
public class LogoutController {

    @PostMapping("logout")
    public MemberLoginStatusResponse logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return MemberLoginInterfaceAssembler.toLogoutResponse();
    }
}
