package com.github.thundax.modules.member.security;

import com.github.thundax.modules.member.utils.ShiroUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 前台会员安全上下文适配器。 */
public final class MemberSecurityContext {

    private MemberSecurityContext() {}

    public static MemberSpringPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof MemberSpringPrincipal) {
            return (MemberSpringPrincipal) authentication.getPrincipal();
        }

        MemberPrincipal shiroPrincipal = ShiroUtils.getPrincipal();
        if (shiroPrincipal == null) {
            return null;
        }
        return new MemberSpringPrincipal(shiroPrincipal.getId());
    }

    public static String getCurrentMemberId() {
        MemberSpringPrincipal principal = getPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.getId();
    }

    public static Object getSessionCache(String name) {
        HttpSession session = currentSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute(name);
    }

    public static void setSessionCache(String name, Object value) {
        HttpSession session = currentSession(true);
        if (session != null) {
            session.setAttribute(name, value);
        }
    }

    private static HttpSession currentSession(boolean create) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        return request.getSession(create);
    }
}
